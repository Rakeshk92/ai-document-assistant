package com.rakesh.backend.service;

import com.rakesh.backend.dto.DocumentResponse;
import com.rakesh.backend.entity.Document;
import com.rakesh.backend.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DocumentResponse uploadDocument(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.endsWith(".txt")) {
            throw new RuntimeException("Only .txt files are supported right now");
        }

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        Document document = new Document(
                originalFileName,
                file.getContentType(),
                content,
                LocalDateTime.now()
        );

        Document savedDocument = documentRepository.save(document);

        return new DocumentResponse(
                savedDocument.getId(),
                savedDocument.getFileName(),
                savedDocument.getFileType(),
                savedDocument.getContent(),
                savedDocument.getUploadedAt()
        );
    }

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(doc -> new DocumentResponse(
                        doc.getId(),
                        doc.getFileName(),
                        doc.getFileType(),
                        doc.getContent(),
                        doc.getUploadedAt()
                ))
                .toList();
    }
}