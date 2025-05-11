package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.QueuedPush;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueuedPushRepository extends MongoRepository<QueuedPush, String> {
    List<QueuedPush> findByIsSentFalse();
}