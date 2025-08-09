package com.poeticjustice.deeppoemsinc.controllers.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poeticjustice.deeppoemsinc.domain.Repository.mongodb.FileMetaRepository;
import com.poeticjustice.deeppoemsinc.domain.Repository.mysql.UserRepository;
import com.poeticjustice.deeppoemsinc.domain.service.FileStorageService;
import com.poeticjustice.deeppoemsinc.domain.service.QuotaService;
import com.poeticjustice.deeppoemsinc.domain.service.SubscriptionValidatorService;
import com.poeticjustice.deeppoemsinc.presentation.controllers.FileUploadController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FileUploadController.class,
            excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class, // Exclude MariaDB/MySQL
                MongoAutoConfiguration.class,      // Exclude MongoDB
                MongoDataAutoConfiguration.class,
                RedisAutoConfiguration.class,      // Exclude Redis
                org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class
            })
public class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionValidatorService subscriptionValidator;

    @MockBean
    private QuotaService quotaService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FileMetaRepository fileMetaRepository;

    @Test
    void uploadFile_success_returnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello".getBytes()
        );

        String requestJson = "{\"userId\":\"user123\",\"category\":\"Docs\",\"client\":\"lettuce\"}";
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.getBytes()
        );

        when(subscriptionValidator.isSubscribed("user123")).thenReturn(true);
        when(quotaService.hasEnoughQuota("user123", file.getSize(), "lettuce")).thenReturn(true);
        when(fileStorageService.storeFile(any(), eq("Docs"), eq("user123")))
                .thenReturn("https://minio.example.com/fileuploader/user123/test.txt");

        mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file)
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File uploaded successfully"))
                .andExpect(jsonPath("$.storageUrl").value("https://minio.example.com/fileuploader/user123/test.txt"));

        verify(quotaService, times(1)).updateQuota("user123", file.getSize(), "lettuce");
    }

    @Test
    void uploadFile_userNotSubscribed_forbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello".getBytes()
        );

        String requestJson = "{\"userId\":\"user123\",\"category\":\"Docs\",\"client\":\"lettuce\"}";
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.getBytes()
        );

        when(subscriptionValidator.isSubscribed("user123")).thenReturn(false);

        mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file)
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User not subscribed"));
    }

    @Test
    void uploadFile_quotaExceeded_badRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello".getBytes()
        );

        String requestJson = "{\"userId\":\"user123\",\"category\":\"Docs\",\"client\":\"lettuce\"}";
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.getBytes()
        );

        when(subscriptionValidator.isSubscribed("user123")).thenReturn(true);
        when(quotaService.hasEnoughQuota("user123", file.getSize(), "lettuce")).thenReturn(false);

        mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file)
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quota exceeded"));
    }

    @Test
    void uploadFile_invalidClientType_badRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello".getBytes()
        );

        String requestJson = "{\"userId\":\"user123\",\"category\":\"Docs\",\"client\":\"invalid\"}";
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.getBytes()
        );

        when(subscriptionValidator.isSubscribed("user123")).thenReturn(true);
        when(quotaService.hasEnoughQuota("user123", file.getSize(), "invalid"))
                .thenThrow(new IllegalArgumentException("Invalid client type: invalid"));

        mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file)
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid client type: invalid"));
    }

    @Test
    void regenerateUrl_success_returnsUrl() throws Exception {
        when(fileStorageService.regeneratePresignedUrl("user123", "test.txt", 1))
                .thenReturn("https://minio.example.com/presigned-url");

        mockMvc.perform(get("/api/v1/files/regenerate-url/user123/test.txt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://minio.example.com/presigned-url"));
    }

    @Test
    void listUserFiles_success_returnsFileUrls() throws Exception {
        when(fileStorageService.listUserFiles("user123", false, 1))
                .thenReturn(List.of("https://minio.example.com/presigned-url1", "https://minio.example.com/presigned-url2"));

        mockMvc.perform(get("/api/v1/files/user/user123")
                        .param("public", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files").isArray())
                .andExpect(jsonPath("$.files[0]").value("https://minio.example.com/presigned-url1"))
                .andExpect(jsonPath("$.files[1]").value("https://minio.example.com/presigned-url2"));
    }
}