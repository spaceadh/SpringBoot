package com.poeticjustice.deeppoemsinc.service;

import com.poeticjustice.deeppoemsinc.models.mongo.QueuedEmail;
import com.poeticjustice.deeppoemsinc.models.mongo.QueuedPush;
import com.poeticjustice.deeppoemsinc.models.mongo.QueuedSMS;
import com.poeticjustice.deeppoemsinc.helpers.TokenReplacementHelper;
import com.poeticjustice.deeppoemsinc.models.mongo.NotificationDocument;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.QueuedEmailRepository;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.QueuedPushRepository;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.QueuedSMSRepository;
import com.mongodb.MongoException;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.NotificationMongoRepository;
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
import java.util.UUID;

@Service
@EnableScheduling
public class NotificationQueueService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationQueueService.class);

    private final NotificationMongoRepository notificationRepository;
    private final QueuedEmailRepository emailRepository;
    private final QueuedSMSRepository smsRepository;
    private final QueuedPushRepository pushRepository;
    private final TokenReplacementHelper tokenReplacementHelper;

    public NotificationQueueService(
            NotificationMongoRepository notificationRepository,
            QueuedEmailRepository emailRepository,
            QueuedSMSRepository smsRepository,
            QueuedPushRepository pushRepository,
            TokenReplacementHelper tokenReplacementHelper) {
        this.notificationRepository = notificationRepository;
        this.emailRepository = emailRepository;
        this.smsRepository = smsRepository;
        this.pushRepository = pushRepository;
        this.tokenReplacementHelper = tokenReplacementHelper;
    }

    @Transactional
    @Retryable(
        value = { MongoException.class, MongoTransactionException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Scheduled(fixedDelayString = "${schedule.interval.seconds}000")
    public void processUnqueuedNotifications() {
        logger.info("Starting processing of unqueued notifications");
        List<NotificationDocument> unqueuedNotifications = notificationRepository.findByIsQueuedFalse();

        if (unqueuedNotifications.isEmpty()) {
            logger.info("No unqueued notifications found");
            return;
        }

        for (NotificationDocument notification : unqueuedNotifications) {
            try {
                processNotification(notification);
                notification.setIsQueued(true);
                notificationRepository.save(notification);
                logger.info("Processed notification with reference: {}", notification.getReference());
            } catch (Exception e) {
                notification.setIsQueued(false);
                notificationRepository.save(notification);
                logger.error("Failed to process notification with reference {}: {}",notification.getReference(), e.getMessage(), e);
                throw e; // Re-throw to trigger retry
            }
        }
        logger.info("Finished processing {} unqueued notifications", unqueuedNotifications.size());
    }

    @Recover
    public void recover(MongoException e, NotificationDocument notification) {
        logger.error("Failed to process notification with reference {} after retries: {}", 
                     notification != null ? notification.getReference() : "unknown", e.getMessage(), e);
        // TODO: Add fallback logic, e.g., save to a dead-letter queue
    }

    private void processNotification(NotificationDocument notification) {
        List<NotificationDocument.Recipient> recipients = notification.getRecipients();
        if (recipients == null || recipients.isEmpty()) {
            logger.warn("No recipients found for notification: {}", notification.getReference());
            return;
        }

        for (NotificationDocument.Recipient recipient : recipients) {
            switch (recipient.getPlatform()) {
                case 2: // Email
                    queueEmail(notification, recipient);
                    break;
                case 1: // SMS
                    queueSMS(notification, recipient);
                    break;
                case 6: // Push
                    queuePush(notification, recipient);
                    break;
                default:
                    logger.warn("Unsupported platform {} for recipient {} in notification {}", 
                                recipient.getPlatform(), recipient.getTo(), notification.getReference());
            }
        }
    }

    private void queueEmail(NotificationDocument notification, NotificationDocument.Recipient recipient) {
        String tokenizedMessage = tokenReplacementHelper.replaceTokens(notification.getMessage(), notification.getTokens());
        String tokenizedSubject = tokenReplacementHelper.replaceTokens(notification.getSubject(), notification.getTokens());
        QueuedEmail email = QueuedEmail.builder()
                .id(UUID.randomUUID().toString())
                .notificationId(notification.getId())
                .reference(notification.getReference())
                .recipient(recipient.getTo())
                .message(tokenizedMessage)
                .subject(tokenizedSubject)
                .tokens(notification.getTokens())
                .hasAttachment(notification.hasAttachment())
                .attachment(notification.getAttachment())
                .productName(notification.getProductName())
                .language(notification.getLanguage())
                .countryCode(notification.getCountryCode())
                .build();
        emailRepository.save(email);
        logger.info("Queued email for recipient {} in notification {}", 
                    recipient.getTo(), notification.getReference());
    }

    private void queueSMS(NotificationDocument notification, NotificationDocument.Recipient recipient) {
        String tokenizedMessage = tokenReplacementHelper.replaceTokens(notification.getMessage(), notification.getTokens());

        QueuedSMS sms = QueuedSMS.builder()
                .id(UUID.randomUUID().toString())
                .notificationId(notification.getId())
                .reference(notification.getReference())
                .recipient(recipient.getTo())
                .message(tokenizedMessage)
                .tokens(notification.getTokens())
                .productName(notification.getProductName())
                .language(notification.getLanguage())
                .countryCode(notification.getCountryCode())
                .build();
        smsRepository.save(sms);
        logger.info("Queued SMS for recipient {} in notification {}", 
                    recipient.getTo(), notification.getReference());
    }

    private void queuePush(NotificationDocument notification, NotificationDocument.Recipient recipient) {
        String tokenizedMessage = tokenReplacementHelper.replaceTokens(notification.getMessage(), notification.getTokens());

        QueuedPush push = QueuedPush.builder()
                .id(UUID.randomUUID().toString())
                .notificationId(notification.getId())
                .reference(notification.getReference())
                .recipient(recipient.getTo())
                .message(tokenizedMessage)
                .tokens(notification.getTokens())
                // .pushDetails(notification.getPushDetails())
                .productName(notification.getProductName())
                .language(notification.getLanguage())
                .countryCode(notification.getCountryCode())
                .build();
        pushRepository.save(push);
        logger.info("Queued push notification for recipient {} in notification {}", 
                    recipient.getTo(), notification.getReference());
    }
}