package com.rakesh.backend.util;

import java.util.ArrayList;
import java.util.List;

public class TextChunker {

    private TextChunker() {
    }

    public static List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        String cleanedText = text.replaceAll("\\s+", " ").trim();

        for (int start = 0; start < cleanedText.length(); start += chunkSize) {
            int end = Math.min(start + chunkSize, cleanedText.length());
            chunks.add(cleanedText.substring(start, end));
        }

        return chunks;
    }
}