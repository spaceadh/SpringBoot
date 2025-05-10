package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.NotificationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface NotificationMongoRepository extends MongoRepository<NotificationDocument, String> {

    @Query("{ 'reference' : ?0 }")
    boolean existsByReference(String reference);
}