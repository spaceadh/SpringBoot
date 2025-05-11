package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.NotificationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface NotificationMongoRepository extends MongoRepository<NotificationDocument, String> {
    @Query(value = "{'reference': ?0}", exists = true)
    Boolean existsByReference(String reference);   
    List<NotificationDocument> findByIsQueuedFalse();
}
// public interface NotificationMongoRepository extends MongoRepository<Notification, String> {
//     Boolean existsByReference(String reference);  // Changed from boolean to Boolean
// }