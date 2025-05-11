package com.poeticjustice.deeppoemsinc.gateways.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poeticjustice.deeppoemsinc.models.mongo.QueuedSMS;
import com.poeticjustice.deeppoemsinc.models.mongo.SMSResponseLogs;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.SMSResponseLogsRepository;
import com.poeticjustice.deeppoemsinc.helpers.GlobalHelper;
import com.poeticjustice.deeppoemsinc.dtos.ATSMSResponse;
import com.poeticjustice.deeppoemsinc.helpers.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class AfricasTalkingGateway implements ISMSGateway {

    private final RestTemplate restTemplate;
    private final Map<String, String> configuration;
    private final Logger logger = LoggerFactory.getLogger(AfricasTalkingGateway.class);
    private final SMSResponseLogsRepository smsResponseLogsRepository;
    private final ValidationHelper validationHelper;
    private final ObjectMapper objectMapper;
    private final int timeoutMillis;

    public AfricasTalkingGateway(RestTemplate restTemplate, Map<String, String> configuration,
                                 SMSResponseLogsRepository smsResponseLogsRepository,
                                 ValidationHelper validationHelper, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.timeoutMillis = Integer.parseInt(
            configuration.getOrDefault("SMS:AfricasTalking:TimeoutMillis", "5000")
        );
        this.configuration = configuration;
        this.smsResponseLogsRepository = smsResponseLogsRepository;
        this.validationHelper = validationHelper;
        this.objectMapper = objectMapper;
    }

    @Retryable(
        value = { HttpClientErrorException.class, RestClientException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    // @CircuitBreaker(
    //     failOn = { RestClientException.class },
    //     resetTimeout = 30000
    // )
    public CompletableFuture<Boolean> sendSMSAsync(QueuedSMS notification) {
        try {
            String formattedPhoneNumber = GlobalHelper.validateATPhoneNumber(notification.getRecipient());
            logger.info("Formatted phone number: {}", formattedPhoneNumber);

            if (!validationHelper.isValidKenyanPhoneNumber(formattedPhoneNumber)) {
                logger.error("Invalid phone number: {}", formattedPhoneNumber);
                return CompletableFuture.completedFuture(false);
            }

            String username = configuration.getOrDefault("SMS:AfricasTalking:" + notification.getProductName() + ":Username", "");
            String apiKey = configuration.getOrDefault("SMS:AfricasTalking:" + notification.getProductName() + ":ApiKey", "");
            String senderId = configuration.getOrDefault("SMS:AfricasTalking:" + notification.getProductName() + ":senderId", "");

            if (username.isEmpty() || apiKey.isEmpty()) {
                logger.error("AfricasTalking credentials missing for product: {}", notification.getProductName());
                return CompletableFuture.completedFuture(false);
            }

            Map<String, String> formData = new HashMap<>();
            formData.put("username", username);
            formData.put("to", formattedPhoneNumber);
            formData.put("message", notification.getMessage());
            formData.put("from", senderId);
            formData.put("enqueue", "1");

            String url = "https://api.africastalking.com/version1/messaging";
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add("apiKey", apiKey);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(formData, headers);

            String responseContent = restTemplate.postForObject(url, request, String.class);
            logger.info("AfricasTalking API Response: {}", responseContent);

            ATSMSResponse smsResponse = objectMapper.readValue(responseContent, ATSMSResponse.class);
            if (smsResponse != null && smsResponse.getSMSMessageData() != null && smsResponse.getSMSMessageData().getRecipients() != null) {
                boolean anySuccess = smsResponse.getSMSMessageData().getRecipients().stream()
                    .anyMatch(r -> r.getStatusCode() == 200 || r.getStatusCode() == 406);
                recordATResponseLogNotification(notification, smsResponse, anySuccess);
                return CompletableFuture.completedFuture(anySuccess);
            }

            logger.error("Invalid response from Africa's Talking API");
            return CompletableFuture.completedFuture(false);
        } catch (Exception ex) {
            logger.error("Error sending SMS via AfricasTalking: {}", ex.getMessage(), ex);
            return CompletableFuture.completedFuture(false);
        }
    }

    private void recordATResponseLogNotification(QueuedSMS notification, ATSMSResponse smsResponse, boolean isSuccess) {
        try {
            SMSResponseLogs smsLog = SMSResponseLogs.builder()
                    .id(UUID.randomUUID().toString())
                    .reference(notification.getReference())
                    .message(notification.getMessage())
                    .recipient(notification.getRecipient())
                    .gateway("AfricasTalking")
                    .isSuccessful(isSuccess)
                    .response(smsResponse.getSMSMessageData().getMessage())
                    .countryCode(notification.getCountryCode())
                    .messageId(smsResponse.getSMSMessageData().getRecipients() != null && !smsResponse.getSMSMessageData().getRecipients().isEmpty()
                        ? smsResponse.getSMSMessageData().getRecipients().get(0).getMessageId() : null)
                    .cost(smsResponse.getSMSMessageData().getRecipients() != null && !smsResponse.getSMSMessageData().getRecipients().isEmpty()
                        ? smsResponse.getSMSMessageData().getRecipients().get(0).getCost() : null)
                    .build();

            smsResponseLogsRepository.save(smsLog);
            logger.info("AT SMS response logged successfully.");
        } catch (Exception ex) {
            logger.error("Error recording AT SMS response log: {}", ex.getMessage(), ex);
        }
    }
}