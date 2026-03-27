package com.rakesh.backend.dto;

public class QuestionResponse {

    private String question;
    private String answer;
    private String sourceDocument;
    private Integer chunkIndex;
    private String mode;

    public QuestionResponse() {
    }

    public QuestionResponse(String question, String answer, String sourceDocument, Integer chunkIndex, String mode) {
        this.question = question;
        this.answer = answer;
        this.sourceDocument = sourceDocument;
        this.chunkIndex = chunkIndex;
        this.mode = mode;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getSourceDocument() {
        return sourceDocument;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public String getMode() {
        return mode;
    }
}