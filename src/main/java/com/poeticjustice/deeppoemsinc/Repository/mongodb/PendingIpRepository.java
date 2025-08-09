package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.PendingIp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PendingIpRepository extends MongoRepository<PendingIp, String> {
    List<PendingIp> findByUserId(String userId);
}