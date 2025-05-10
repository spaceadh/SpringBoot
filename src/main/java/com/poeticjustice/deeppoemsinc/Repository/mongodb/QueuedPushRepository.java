package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.QueuedPush;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueuedPushRepository extends MongoRepository<QueuedPush, String> {
}