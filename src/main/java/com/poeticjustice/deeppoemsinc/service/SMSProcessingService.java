package com.poeticjustice.deeppoemsinc.service;

import com.poeticjustice.deeppoemsinc.models.mongo.DeadLetterEntry;
import com.poeticjustice.deeppoemsinc.models.mongo.QueuedSMS;
import com.mongodb.MongoException;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.DeadLetterRepository;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.QueuedSMSRepository;
import com.poeticjustice.deeppoemsinc.gateways.sms.SMSGatewayFactory;
import com.poeticjustice.deeppoemsinc.gateways.sms.ISMSGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoTransactionException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@EnableScheduling
public class SMSProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(SMSProcessingService.class);

    private final QueuedSMSRepository smsRepository;
    private final SMSGatewayFactory gatewayFactory;
    private final DeadLetterRepository deadLetterRepository;

    public SMSProcessingService(QueuedSMSRepository smsRepository, SMSGatewayFactory gatewayFactory,
                               DeadLetterRepository deadLetterRepository) {
        this.smsRepository = smsRepository;
        this.gatewayFactory = gatewayFactory;
        this.deadLetterRepository = deadLetterRepository;
    }

    @Transactional
    @Retryable(
        value = { MongoException.class, MongoTransactionException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Scheduled(fixedDelayString = "${sms.process.interval.seconds}000")
    public void processUnsentSMS() {
        logger.info("Starting processing of unsent SMS");
        List<QueuedSMS> unsentSMS = smsRepository.findByIsSentFalse();

        if (unsentSMS.isEmpty()) {
            logger.info("No unsent SMS found");
            return;
        }

        for (QueuedSMS sms : unsentSMS) {
            try {
                ISMSGateway gateway = gatewayFactory.getSMSGateway(sms.getCountryCode());
                if (gateway == null) {
                    logger.error("No SMS gateway available for country code: {}", sms.getCountryCode());
                    continue;
                }

                CompletableFuture<Boolean> sendFuture = gateway.sendSMSAsync(sms);
                boolean sent = sendFuture.join(); // Block until completion
                if (sent) {
                    sms.setSent(true);
                    smsRepository.save(sms);
                    logger.info("Successfully sent SMS with reference: {}", sms.getReference());
                } else {
                    sms.setSent(false);
                    smsRepository.save(sms);
                    logger.warn("Failed to send SMS with reference: {}", sms.getReference());
                }
            } catch (Exception e) {
                sms.setSent(false);
                smsRepository.save(sms);    
                logger.error("Failed to process SMS with reference {}: {}", sms.getReference(), e.getMessage(), e);
                throw e; // Re-throw to trigger retry
            }
        }
        logger.info("Finished processing {} unsent SMS", unsentSMS.size());
    }

    @Scheduled(fixedDelayString = "${dlq.retry.interval.seconds}000")
    public void retryDeadLetterQueue() {
        List<DeadLetterEntry> entries = deadLetterRepository.findAll();
        for (DeadLetterEntry entry : entries) {
            QueuedSMS sms = QueuedSMS.builder()
                .reference(entry.getReference())
                .recipient(entry.getRecipient())
                .message(entry.getMessage())
                .countryCode(entry.getCountryCode())
                .isSent(false)
                .build();
            smsRepository.save(sms);
            deadLetterRepository.delete(entry);
            logger.info("Moved DLQ entry {} back to QueuedSMS", entry.getReference());
        }
    }
    @Recover
    public void recover(MongoException e, QueuedSMS sms) {
        logger.error("Failed to process SMS with reference {} after retries: {}", 
                     sms != null ? sms.getReference() : "unknown", e.getMessage(), e);
        if (sms != null) {
            DeadLetterEntry entry = DeadLetterEntry.builder()
                .reference(sms.getReference())
                .recipient(sms.getRecipient())
                .message(sms.getMessage())
                .countryCode(sms.getCountryCode())
                .errorMessage(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
            deadLetterRepository.save(entry);
            logger.info("Saved failed SMS with reference {} to dead-letter queue", sms.getReference());
        }
    }
}