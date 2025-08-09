package com.poeticjustice.deeppoemsinc.domain.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.domain.models.mongo.DeadLetterEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeadLetterRepository extends MongoRepository<DeadLetterEntry, String> {
    // Custom query methods can be added here if needed
}