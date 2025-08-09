package com.poeticjustice.deeppoemsinc.domain.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.domain.models.mongo.AcceptedIps;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AcceptedIpsRepository extends MongoRepository<AcceptedIps, String> {
    List<AcceptedIps> findByUserId(String userId);

    AcceptedIps findByUserIdAndIpAddress(String userId, String ipAddress);

    // Custom method signatures for update/add operations
    // Note: Actual implementation should be in a service or with @Query/@Modifying if needed

    // Example: Add a new AcceptedIps document
    <S extends AcceptedIps> S save(S entity);

    // Example: Update ipAddress for a given userId (requires custom implementation)
    // void updateIpAddressByUserId(String userId, String newIpAddress);
}