package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.QueuedSMS;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueuedSMSRepository extends MongoRepository<QueuedSMS, String> {
    List<QueuedSMS> findByIsSentFalse();
}