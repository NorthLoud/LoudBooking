package com.example.loudhotel.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatUserResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String avatar;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private Long hotelId;
    private String hotelName;
    private String hotelImage;
}