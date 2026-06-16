package com.example.loudhotel.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private Long recipientId;
    private Long hotelId;
    private String content;
    private List<String> images;
    private Long replyToId;
}