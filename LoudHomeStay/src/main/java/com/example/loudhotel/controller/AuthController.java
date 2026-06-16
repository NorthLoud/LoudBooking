package com.example.loudhotel.controller;

import com.example.loudhotel.dto.request.LoginRequest;
import com.example.loudhotel.dto.request.RefreshTokenRequest;
import com.example.loudhotel.dto.request.RegisterRequest;
import com.example.loudhotel.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Object login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/refresh-token")
    public Object refreshToken(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }

}
