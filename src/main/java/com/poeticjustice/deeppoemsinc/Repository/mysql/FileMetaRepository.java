package com.poeticjustice.deeppoemsinc.Repository.mysql;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poeticjustice.deeppoemsinc.models.mysql.FileMeta;

@Repository
public interface FileMetaRepository extends JpaRepository<FileMeta, Long> {

    List<FileMeta> findByUserId(String userId);

    List<FileMeta> findByExpiryDateBefore(LocalDateTime now);
    
}
