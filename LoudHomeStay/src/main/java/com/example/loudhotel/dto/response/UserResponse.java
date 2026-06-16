package com.example.loudhotel.dto.response;

import com.example.loudhotel.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String firstName;
    private String lastName;
    private User.Status status;
    private String fullName;
}
