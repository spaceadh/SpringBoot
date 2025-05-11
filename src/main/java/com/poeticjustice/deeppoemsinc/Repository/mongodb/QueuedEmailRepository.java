package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.QueuedEmail;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueuedEmailRepository extends MongoRepository<QueuedEmail, String> {
    List<QueuedEmail> findByIsSentFalse();
}