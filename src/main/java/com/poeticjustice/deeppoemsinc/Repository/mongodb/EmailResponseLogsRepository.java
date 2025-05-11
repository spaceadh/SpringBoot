package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.EmailResponseLogs;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailResponseLogsRepository extends MongoRepository<EmailResponseLogs, String> {
}