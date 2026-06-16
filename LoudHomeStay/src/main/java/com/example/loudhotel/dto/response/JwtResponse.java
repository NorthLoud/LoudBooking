package com.example.loudhotel.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private Long userId;
    private String username;
    private String email;
    private String role;

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
