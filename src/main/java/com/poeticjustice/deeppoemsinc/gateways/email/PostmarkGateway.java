package com.poeticjustice.deeppoemsinc.gateways.email;

import com.poeticjustice.deeppoemsinc.Repository.mongodb.EmailResponseLogsRepository;
import com.poeticjustice.deeppoemsinc.models.mongo.EmailResponseLogs;
import com.poeticjustice.deeppoemsinc.models.mongo.QueuedEmail;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class PostmarkGateway implements IEmailGateway {

    private static final Logger logger = LoggerFactory.getLogger(PostmarkGateway.class);
    private final EmailResponseLogsRepository emailResponseLogsRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiToken;
    private final String senderEmail;
    private final String messageStream;

    public PostmarkGateway(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            EmailResponseLogsRepository emailResponseLogsRepository,
            @Value("${Email.Postmark.Chamasoft.ApiToken}") String apiToken,
            @Value("${Email.Postmark.Chamasoft.SenderEmail}") String senderEmail,
            @Value("${Email.Postmark.Chamasoft.MessageStream}") String messageStream) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.emailResponseLogsRepository = emailResponseLogsRepository;
        this.apiToken = apiToken;
        this.senderEmail = senderEmail;
        this.messageStream = messageStream;
    }

    @Retryable(
        value = { HttpClientErrorException.class, RestClientException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public CompletableFuture<Boolean> sendEmailAsync(QueuedEmail email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = "https://api.postmarkapp.com/email";

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("From", senderEmail);
                requestBody.put("To", email.getRecipient());
                requestBody.put("Subject", email.getSubject());
                requestBody.put("TextBody", email.getMessage());
                requestBody.put("MessageStream", messageStream);

                if (email.isHasAttachment() && email.getAttachment() != null) {
                    List<Map<String, String>> attachments = email.getAttachment().stream()
                        .map(attachment -> {
                            Map<String, String> att = new HashMap<>();
                            att.put("Name", attachment.getFileName());
                            att.put("Content", attachment.getFileType());
                            att.put("ContentType", attachment.getBase64Attachment());
                            return att;
                        })
                        .collect(Collectors.toList());
                    requestBody.put("Attachments", attachments);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Postmark-Server-Token", apiToken);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

                logger.info("Postmark API Response: {}", response.getBody());

                if (response.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                    boolean isSuccessful = responseBody.get("ErrorCode").equals(0);
                    recordResponseLog(email, responseBody, isSuccessful);
                    return isSuccessful;
                }

                logger.error("HTTP error from Postmark API: {}", response.getStatusCode());
                recordResponseLog(email, null, false);
                return false;
            } catch (Exception ex) {
                logger.error("Error sending email via Postmark: {}", ex.getMessage(), ex);
                recordResponseLog(email, null, false);
                throw new RuntimeException(ex);
            }
        });
    }

    private void recordResponseLog(QueuedEmail email, Map<String, Object> responseBody, boolean isSuccessful) {
        try {
            EmailResponseLogs log = EmailResponseLogs.builder()
                    .id(UUID.randomUUID().toString())
                    .reference(email.getReference())
                    .recipient(email.getRecipient())
                    .gateway("Postmark")
                    .isSuccessful(isSuccessful)
                    .response(responseBody != null ? responseBody.toString() : "Error")
                    .messageId(responseBody != null ? (String) responseBody.get("MessageID") : null)
                    .build();

            emailResponseLogsRepository.save(log);
            logger.info("Postmark response logged successfully for reference: {}", email.getReference());
        } catch (Exception ex) {
            logger.error("Error recording Postmark response log: {}", ex.getMessage(), ex);
        }
    }
}