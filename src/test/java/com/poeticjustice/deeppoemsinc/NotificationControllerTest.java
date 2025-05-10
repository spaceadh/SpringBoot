package com.poeticjustice.deeppoemsinc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateNotification() throws Exception {
        String json = "{\"reference\":\"AS-2342-175665769\",\"recipients\":[{\"to\":\"test@example.com\",\"platform\":2}],\"message\":\"Test message\",\"subject\":\"Test subject\",\"tokens\":{\"recipientName\":\"Test1\"},\"hasAttachment\":false,\"productName\":\"Chamasoft\",\"language\":\"en\",\"countryCode\":\"KE\"}";
        mockMvc.perform(post("/api/notifications")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());
    }
}