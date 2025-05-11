package com.poeticjustice.deeppoemsinc.gateways;

import com.poeticjustice.deeppoemsinc.models.mongo.QueuedSMS;
import com.poeticjustice.deeppoemsinc.models.mongo.SMSResponseLogs;
import com.poeticjustice.deeppoemsinc.util.GlobalHelper;
import com.poeticjustice.deeppoemsinc.util.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class AfricasTalkingGateway implements ISMSGateway {

    private final HttpClient httpClient;
    private final Map<String, String> configuration;
    private final Logger logger;
    private final MongoDbContext dbContext;
    private final ValidationHelper validationHelper;

    public AfricasTalkingGateway(HttpClient httpClient, Map<String, String> configuration, Logger<AfricasTalkingGateway> logger, MongoDbContext dbContext, ValidationHelper validationHelper) {
        this.httpClient = httpClient;
        this.configuration = configuration;
        this.logger = logger;
        this.dbContext = dbContext;
        this.validationHelper = validationHelper;
    }

    @Retryable(
        value = { HttpClientErrorException.class, RestClientException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public async Task<bool> SendSMSAsync(QueuedSMS notification) {
        try {
            String formattedPhoneNumber = GlobalHelper.validateATPhoneNumber(notification.getRecipient());
            logger.info("Formatted phone number: {}", formattedPhoneNumber);

            if (!validationHelper.isValidKenyanPhoneNumber(formattedPhoneNumber)) {
                logger.error("Invalid phone number: {}", formattedPhoneNumber);
                return false;
            }

            String username = configuration.getOrDefault("SMS:AfricasTalking:" + notification.getProductName() + ":Username", "");
            String apiKey = configuration.getOrDefault("SMS:AfricasTalking:" + notification.getProductName() + ":ApiKey", "");
            String senderId = configuration.getOrDefault("SMS:AfricasTalking:" + notification.getProductName() + ":senderId", "");

            if (username.isEmpty() || apiKey.isEmpty()) {
                logger.error("AfricasTalking credentials missing for product: {}", notification.getProductName());
                return false;
            }

            Map<String, String> formData = new HashMap<>();
            formData.put("username", username);
            formData.put("to", formattedPhoneNumber);
            formData.put("message", notification.getMessage());
            formData.put("from", senderId);
            formData.put("enqueue", "1");

            String url = "https://api.africastalking.com/version1/messaging?username=" + username;
            httpClient.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            httpClient.getHeaders().add("apikey", apiKey);

            HttpResponse response = httpClient.post(url, formData);
            String responseContent = response.getContent();

            logger.info("AfricasTalking API Response: {}", responseContent);

            if (response.isSuccessStatusCode()) {
                ATSMSResponse smsResponse = JsonSerializer.deserialize(responseContent, ATSMSResponse.class, new JsonSerializerOptions(true));

                if (smsResponse != null && smsResponse.getSMSMessageData() != null && smsResponse.getSMSMessageData().getRecipients() != null) {
                    boolean anySuccess = smsResponse.getSMSMessageData().getRecipients().stream().anyMatch(r -> r.getStatusCode() == 200 || r.getStatusCode() == 406);
                    await recordATResponseLogNotificationAsync(notification, smsResponse, anySuccess);
                    return anySuccess;
                }
            }

            logger.error("HTTP error from Africa's Talking API: {}", response.getStatusCode());
            return false;
        } catch (Exception ex) {
            logger.error("Error sending SMS via AfricasTalking: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private async Task recordATResponseLogNotificationAsync(QueuedSMS notification, ATSMSResponse smsResponse, boolean isSuccess) {
        try {
            SMSResponseLogs smsLog = SMSResponseLogs.builder()
                    .reference(notification.getReference())
                    .message(notification.getMessage())
                    .recipient(notification.getRecipient())
                    .gateway("AfricasTalking")
                    .isSuccessful(isSuccess)
                    .response(smsResponse.getSMSMessageData().getMessage())
                    .countryCode(notification.getCountryCode())
                    .messageId(smsResponse.getSMSMessageData().getRecipients() != null && !smsResponse.getSMSMessageData().getRecipients().isEmpty() ? smsResponse.getSMSMessageData().getRecipients().get(0).getMessageId() : null)
                    .cost(smsResponse.getSMSMessageData().getRecipients() != null && !smsResponse.getSMSMessageData().getRecipients().isEmpty() ? smsResponse.getSMSMessageData().getRecipients().get(0).getCost() : null)
                    .build();

            dbContext.getSMSResponseLogs().insertOne(smsLog);
            logger.info("AT SMS response logged successfully.");
        } catch (Exception ex) {
            logger.error("Error recording AT SMS response log: {}", ex.getMessage(), ex);
        }
    }
}