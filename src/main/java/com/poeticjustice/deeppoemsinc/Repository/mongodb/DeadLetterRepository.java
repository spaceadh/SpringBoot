package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.DeadLetterEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeadLetterRepository extends MongoRepository<DeadLetterEntry, String> {
}