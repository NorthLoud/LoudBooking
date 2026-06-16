package com.example.loudhotel.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatResponse {
    private Long chatId;
    private Long senderId;
    private Long recipientId;
    private Long hotelId;
    private String content;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isEdited;
    private Boolean isRead;
    private Boolean isDeleted;
    private String replyToContent;
    private String replyToUser;
    private Long replyToId;
}