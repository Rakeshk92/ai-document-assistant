package com.rakesh.backend.controller;

import com.rakesh.backend.dto.DocumentResponse;
import com.rakesh.backend.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        DocumentResponse response = documentService.uploadDocument(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }
}