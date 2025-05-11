package com.poeticjustice.deeppoemsinc.service;

import com.poeticjustice.deeppoemsinc.gateways.GatewayFactory;
import com.poeticjustice.deeppoemsinc.gateways.ISMSGateway;
import com.poeticjustice.deeppoemsinc.models.mongo.QueuedSMS;
import com.mongodb.MongoException;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.QueuedSMSRepository;
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
// import org.springframework.data.mongodb.core.MongoException;

import java.util.List;

@Service
@EnableScheduling
public class SMSProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(SMSProcessingService.class);

    private final QueuedSMSRepository smsRepository;
    private final GatewayFactory gatewayFactory;

    public SMSProcessingService(QueuedSMSRepository smsRepository, GatewayFactory gatewayFactory) {
        this.smsRepository = smsRepository;
        this.gatewayFactory = gatewayFactory;
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

                boolean sent = gateway.sendSMSAsync(sms).get();
                if (sent) {
                    sms.setSent(true);
                    smsRepository.save(sms);
                    logger.info("Successfully sent SMS with reference: {}", sms.getReference());
                } else {
                    logger.warn("Failed to send SMS with reference: {}", sms.getReference());
                }
            } catch (Exception e) {
                logger.error("Failed to process SMS with reference {}: {}", sms.getReference(), e.getMessage(), e);
                throw e; // Re-throw to trigger retry
            }
        }
        logger.info("Finished processing {} unsent SMS", unsentSMS.size());
    }

    @Recover
    public void recover(MongoException e, QueuedSMS sms) {
        logger.error("Failed to process SMS with reference {} after retries: {}", 
                     sms != null ? sms.getReference() : "unknown", e.getMessage(), e);
        // TODO: Save to a dead-letter queue if needed
    }
}