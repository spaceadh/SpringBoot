package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.SMSResponseLogs;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SMSResponseLogsRepository extends MongoRepository<SMSResponseLogs, String> {
}