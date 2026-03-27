package com.rakesh.backend.repository;

import com.rakesh.backend.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
}