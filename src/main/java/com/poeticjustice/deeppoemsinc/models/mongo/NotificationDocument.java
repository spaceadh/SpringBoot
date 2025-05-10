// package com.poeticjustice.deeppoemsinc.models.mongo;

// import org.springframework.data.annotation.Id;
// import org.springframework.data.mongodb.core.index.Indexed;
// import org.springframework.data.mongodb.core.mapping.Document;

// import java.util.List;
// import java.util.Map;

// @Document(collection = "Notifications")
// public class NotificationDocument {

//     @Id
//     private String id;

//     @Indexed(unique = true)
//     private String reference;

//     private List<Recipient> recipients;
//     private String message;
//     private String subject;
//     private Map<String, String> tokens;
//     private Boolean hasAttachment;
//     private List<Attachment> attachment;
//     private String productName;
//     private String language;
//     private Object pushDetails;
//     private String countryCode;

//     public static class Recipient {
//         private String to;
//         private Integer platform;

//         public String getTo() {
//             return to;
//         }

//         public void setTo(String to) {
//             this.to = to;
//         }

//         public Integer getPlatform() {
//             return platform;
//         }

//         public void setPlatform(Integer platform) {
//             this.platform = platform;
//         }
//     }

//     public static class Attachment {
//         private String fileName;
//         private String fileType;
//         private String base64Attachment;

//         public String getFileName() {
//             return fileName;
//         }

//         public void setFileName(String fileName) {
//             this.fileName = fileName;
//         }

//         public String getFileType() {
//             return fileType;
//         }

//         public void setFileType(String fileType) {
//             this.fileType = fileType;
//         }

//         public String getBase64Attachment() {
//             return base64Attachment;
//         }

//         public void setBase64Attachment(String base64Attachment) {
//             this.base64Attachment = base64Attachment;
//         }
//     }

//     public String getId() {
//         return id;
//     }

//     public void setId(String id) {
//         this.id = id;
//     }

//     public String getReference() {
//         return reference;
//     }

//     public void setReference(String reference) {
//         this.reference = reference;
//     }

//     public List<Recipient> getRecipients() {
//         return recipients;
//     }

//     public void setRecipients(List<Recipient> recipients) {
//         this.recipients = recipients;
//     }

//     public String getMessage() {
//         return message;
//     }

//     public void setMessage(String message) {
//         this.message = message;
//     }

//     public String getSubject() {
//         return subject;
//     }

//     public void setSubject(String subject) {
//         this.subject = subject;
//     }

//     public Map<String, String> getTokens() {
//         return tokens;
//     }

//     public void setTokens(Map<String, String> tokens) {
//         this.tokens = tokens;
//     }

//     public Boolean getHasAttachment() {
//         return hasAttachment;
//     }

//     public void setHasAttachment(Boolean hasAttachment) {
//         this.hasAttachment = hasAttachment;
//     }

//     public List<Attachment> getAttachment() {
//         return attachment;
//     }

//     public void setAttachment(List<Attachment> attachment) {
//         this.attachment = attachment;
//     }

//     public String getProductName() {
//         return productName;
//     }

//     public void setProductName(String productName) {
//         this.productName = productName;
//     }

//     public String getLanguage() {
//         return language;
//     }

//     public void setLanguage(String language) {
//         this.language = language;
//     }

//     public Object getPushDetails() {
//         return pushDetails;
//     }

//     public void setPushDetails(Object pushDetails) {
//         this.pushDetails = pushDetails;
//     }

//     public String getCountryCode() {
//         return countryCode;
//     }

//     public void setCountryCode(String countryCode) {
//         this.countryCode = countryCode;
//     }
// }
package com.poeticjustice.deeppoemsinc.models.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Notifications")
public class NotificationDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String reference;

    private List<Recipient> recipients;
    private String message;
    private String subject;
    private Map<String, String> tokens;
    private Boolean hasAttachment;
    private List<Attachment> attachment;
    private String productName;
    private String language;
    private Object pushDetails;
    private String countryCode;
    
    @Builder.Default
    private boolean isQueued = false;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Recipient {
        private String to;
        private Integer platform;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attachment {
        private String fileName;
        private String fileType;
        private String base64Attachment;
    }
}