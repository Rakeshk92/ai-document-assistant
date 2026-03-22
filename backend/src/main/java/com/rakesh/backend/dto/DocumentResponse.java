package com.rakesh.backend.dto;

import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private String content;
    private LocalDateTime uploadedAt;

    public DocumentResponse() {
    }

    public DocumentResponse(Long id, String fileName, String fileType, String content, LocalDateTime uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.content = content;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}