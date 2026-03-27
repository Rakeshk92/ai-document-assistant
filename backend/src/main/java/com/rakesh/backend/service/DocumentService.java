package com.rakesh.backend.service;

import com.rakesh.backend.dto.DocumentResponse;
import com.rakesh.backend.dto.QuestionResponse;
import com.rakesh.backend.entity.Document;
import com.rakesh.backend.entity.DocumentChunk;
import com.rakesh.backend.repository.DocumentChunkRepository;
import com.rakesh.backend.repository.DocumentRepository;
import com.rakesh.backend.util.TextChunker;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentChunkRepository documentChunkRepository) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
    }

    public DocumentResponse uploadDocument(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new RuntimeException("Invalid file name");
        }

        String lowerFileName = originalFileName.toLowerCase();
        String content;

        if (lowerFileName.endsWith(".txt")) {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } else if (lowerFileName.endsWith(".pdf")) {
            content = extractTextFromPdf(file);
        } else {
            throw new RuntimeException("Only .txt and .pdf files are supported right now");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Could not extract content from file");
        }

        Document document = new Document(
                originalFileName,
                file.getContentType(),
                content,
                LocalDateTime.now()
        );

        Document savedDocument = documentRepository.save(document);
        saveChunks(savedDocument.getId(), content);

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
                .collect(Collectors.toList());
    }

    public DocumentResponse getDocumentById(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        return new DocumentResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getFileType(),
                doc.getContent(),
                doc.getUploadedAt()
        );
    }

    public List<DocumentResponse> searchDocuments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new RuntimeException("Search keyword cannot be empty");
        }

        return documentRepository.findByContentContainingIgnoreCase(keyword)
                .stream()
                .map(doc -> new DocumentResponse(
                        doc.getId(),
                        doc.getFileName(),
                        doc.getFileType(),
                        doc.getContent(),
                        doc.getUploadedAt()
                ))
                .collect(Collectors.toList());
    }

    public QuestionResponse askQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            throw new RuntimeException("Question cannot be empty");
        }

        List<DocumentChunk> allChunks = documentChunkRepository.findAll();

        if (allChunks.isEmpty()) {
            throw new RuntimeException("No document chunks available. Please upload a document first.");
        }

        String[] questionWords = question.toLowerCase().split("\\W+");

        DocumentChunk bestChunk = null;
        int bestScore = 0;

        for (DocumentChunk chunk : allChunks) {
            String chunkTextLower = chunk.getChunkText().toLowerCase();
            int score = 0;

            for (String word : questionWords) {
                if (!word.isBlank() && chunkTextLower.contains(word)) {
                    score++;
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestChunk = chunk;
            }
        }

        if (bestChunk == null || bestScore == 0) {
            return new QuestionResponse(
                    question,
                    "I could not find a relevant answer in the uploaded documents.",
                    null,
                    null,
                    "Fallback Retrieval"
            );
        }

        Document sourceDocument = documentRepository.findById(bestChunk.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Source document not found"));

        String answer = extractBestSentence(bestChunk.getChunkText(), questionWords);

        return new QuestionResponse(
                question,
                answer,
                sourceDocument.getFileName(),
                bestChunk.getChunkIndex(),
                "Fallback Retrieval"
        );
    }

    private void saveChunks(Long documentId, String content) {
        List<String> chunks = TextChunker.chunkText(content, 500);

        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = new DocumentChunk(documentId, i, chunks.get(i));
            documentChunkRepository.save(chunk);
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument pdfDocument = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return pdfTextStripper.getText(pdfDocument);
        }
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        documentChunkRepository.deleteByDocumentId(id);
        documentRepository.delete(document);
    }

    private String extractBestSentence(String chunkText, String[] questionWords) {
        String[] sentences = chunkText.split("(?<=[.!?])\\s+");

        String bestSentence = chunkText;
        int bestScore = 0;

        for (String sentence : sentences) {
            String lowerSentence = sentence.toLowerCase();
            int score = 0;

            for (String word : questionWords) {
                if (!word.isBlank() && lowerSentence.contains(word)) {
                    score++;
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestSentence = sentence.trim();
            }
        }

        return bestSentence;
    }
}