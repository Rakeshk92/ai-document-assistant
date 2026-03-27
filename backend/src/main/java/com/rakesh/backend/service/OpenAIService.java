package com.rakesh.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.embedding.model:}")
    private String embeddingModel;

    @Value("${openai.chat.model:}")
    private String chatModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean isConfigured() {
        return isRealValue(apiKey)
                && isRealValue(embeddingModel)
                && isRealValue(chatModel);
    }

    private boolean isRealValue(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String lower = value.toLowerCase();
        return !lower.contains("your_key_here")
                && !lower.contains("paste_your_real_openai_key_here")
                && !lower.contains("your_embedding_model_here")
                && !lower.contains("your_chat_model_here");
    }

    public List<Double> createEmbedding(String inputText) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", embeddingModel);
            payload.put("input", inputText);

            String body = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/embeddings"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Embedding API failed: " + response.body());
            }

            Map<String, Object> responseMap = objectMapper.readValue(
                    response.body(),
                    new TypeReference<Map<String, Object>>() {}
            );

            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
            if (data == null || data.isEmpty()) {
                return Collections.emptyList();
            }

            List<Number> rawEmbedding = (List<Number>) data.get(0).get("embedding");
            List<Double> embedding = new ArrayList<>();

            for (Number number : rawEmbedding) {
                embedding.add(number.doubleValue());
            }

            return embedding;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create embedding: " + e.getMessage(), e);
        }
    }

    public String generateAnswer(String question, List<String> contextChunks) {
        if (!isConfigured()) {
            return "OpenAI is not configured. Using fallback retrieval mode.";
        }

        try {
            StringBuilder contextBuilder = new StringBuilder();
            for (int i = 0; i < contextChunks.size(); i++) {
                contextBuilder.append("Chunk ").append(i + 1).append(":\n")
                        .append(contextChunks.get(i))
                        .append("\n\n");
            }

            String prompt = """
                    You are a document assistant.
                    Answer only from the provided context.
                    Keep the answer concise and accurate.
                    If the answer is not in the context, say that clearly.

                    Context:
                    %s

                    Question:
                    %s
                    """.formatted(contextBuilder, question);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", chatModel);
            payload.put("input", prompt);

            String body = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Responses API failed: " + response.body());
            }

            Map<String, Object> responseMap = objectMapper.readValue(
                    response.body(),
                    new TypeReference<Map<String, Object>>() {}
            );

            Object outputText = responseMap.get("output_text");
            if (outputText != null) {
                return outputText.toString();
            }

            return "No answer returned by the model.";
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate answer: " + e.getMessage(), e);
        }
    }

    public String embeddingToJson(List<Double> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize embedding", e);
        }
    }

    public List<Double> embeddingFromJson(String embeddingJson) {
        try {
            if (embeddingJson == null || embeddingJson.isBlank()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(embeddingJson, new TypeReference<List<Double>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize embedding", e);
        }
    }

    public double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty() || a.size() != b.size()) {
            return -1.0;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }

        if (normA == 0.0 || normB == 0.0) {
            return -1.0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}