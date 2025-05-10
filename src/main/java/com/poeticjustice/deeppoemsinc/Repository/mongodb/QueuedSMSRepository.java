package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.QueuedSMS;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueuedSMSRepository extends MongoRepository<QueuedSMS, String> {
}