package com.example.loudhotel.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ChatbotRequest {
    private String message;
    private List<Message> history;

    @Data
    public static class Message {
        private String role; // "user" hoặc "assistant"
        private String content;
    }
}
