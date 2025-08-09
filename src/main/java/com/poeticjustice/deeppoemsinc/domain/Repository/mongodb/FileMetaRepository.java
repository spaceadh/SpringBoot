package com.poeticjustice.deeppoemsinc.domain.Repository.mongodb;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poeticjustice.deeppoemsinc.domain.models.mongo.FileMeta;

@Repository
public interface FileMetaRepository extends JpaRepository<FileMeta, Long> {

    List<FileMeta> findByUserId(String userId);

    List<FileMeta> findByExpiryDateBefore(LocalDateTime now);
    
}
