package com.poeticjustice.deeppoemsinc.gateways.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poeticjustice.deeppoemsinc.models.mongo.QueuedSMS;
import com.poeticjustice.deeppoemsinc.models.mongo.SMSResponseLogs;

import io.github.cdimascio.dotenv.Dotenv;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    public CompletableFuture<Boolean> sendSMSAsync(QueuedSMS notification) {
        try {            
            String formattedPhoneNumber = GlobalHelper.validateATPhoneNumber(notification.getRecipient());
            logger.info("Formatted phone number: {}", formattedPhoneNumber);

            Dotenv dotenv = Dotenv.configure().load();
            String usernameKey = "SMS_AF_" + notification.getProductName().toUpperCase() + "_USERNAME";
            String apiKeyKey = "SMS_AF_" + notification.getProductName().toUpperCase() + "_APIKEY";
            String senderIdKey = "SMS_AF_" + notification.getProductName().toUpperCase() + "_SENDERID";

            // Get the values
            String username = dotenv.get(usernameKey);
            String apiKey = dotenv.get(apiKeyKey);
            String senderId = dotenv.get(senderIdKey);

            if (username == null || apiKey == null || username.isEmpty() || apiKey.isEmpty()) {
                logger.error("AfricasTalking credentials missing for product: {}", notification.getProductName());
                return CompletableFuture.completedFuture(false);
            }

            String url = "https://api.africastalking.com/version1/messaging?username=" + username;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("apiKey", apiKey);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("username", username);
            formData.add("to", formattedPhoneNumber);
            formData.add("message", notification.getMessage());
            formData.add("from", senderId);
            formData.add("enqueue", "1");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);


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