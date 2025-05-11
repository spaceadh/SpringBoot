package com.poeticjustice.deeppoemsinc.service;

import com.poeticjustice.deeppoemsinc.gateways.email.EmailGatewayFactory;
import com.poeticjustice.deeppoemsinc.gateways.email.IEmailGateway;
import com.poeticjustice.deeppoemsinc.models.mongo.DeadLetterEntry;
import com.poeticjustice.deeppoemsinc.models.mongo.QueuedEmail;
import com.mongodb.MongoException;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.DeadLetterRepository;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.QueuedEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.MongoTransactionException;

import java.util.List;

@Service
@EnableScheduling
public class EmailProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(EmailProcessingService.class);

    private final QueuedEmailRepository emailRepository;
    private final EmailGatewayFactory gatewayFactory;
    private final DeadLetterRepository deadLetterRepository;

    public EmailProcessingService(QueuedEmailRepository emailRepository, 
    EmailGatewayFactory gatewayFactory,DeadLetterRepository deadLetterRepository) {
        this.emailRepository = emailRepository;
        this.gatewayFactory = gatewayFactory;
        this.deadLetterRepository = deadLetterRepository;
    }

    @Transactional
    @Retryable(
        value = { MongoException.class, MongoTransactionException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Scheduled(fixedDelayString = "${email.process.interval.seconds}000")
    public void processUnsentEmails() {
        logger.info("Starting processing of unsent emails");
        List<QueuedEmail> unsentEmails = emailRepository.findByIsSentFalse();

        if (unsentEmails.isEmpty()) {
            logger.info("No unsent emails found");
            return;
        }

        for (QueuedEmail email : unsentEmails) {
            try {
                IEmailGateway gateway = gatewayFactory.getEmailGateway(email.getCountryCode());
                if (gateway == null) {
                    logger.error("No email gateway available for country code: {}", email.getCountryCode());
                    continue;
                }

                Boolean sent = gateway.sendEmailAsync(email).join();
                if (sent) {
                    email.setSent(true);
                    emailRepository.save(email);
                    logger.info("Successfully sent email with reference: {}", email.getReference());
                } else {
                    logger.warn("Failed to send email with reference: {}", email.getReference());
                }
            } catch (Exception e) {
                logger.error("Failed to process email with reference {}: {}", email.getReference(), e.getMessage(), e);
                throw e;
            }
        }
        logger.info("Finished processing {} unsent emails", unsentEmails.size());
    }

    @Recover
    public void recover(MongoException e, QueuedEmail email) {
        logger.error("Failed to process email with reference {} after retries: {}", 
                     email != null ? email.getReference() : "unknown", e.getMessage(), e);
        if (email != null) {
             DeadLetterEntry entry = DeadLetterEntry.builder()
                .reference(email.getReference())
                .recipient(email.getRecipient())
                .message(email.getMessage())
                .countryCode(email.getCountryCode())
                .errorMessage(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
            deadLetterRepository.save(entry);
            logger.info("Saved failed SMS with reference {} to dead-letter queue", email.getReference());
        }
    }
}