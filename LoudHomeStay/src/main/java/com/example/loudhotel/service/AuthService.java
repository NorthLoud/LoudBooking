package com.example.loudhotel.service;

import com.example.loudhotel.dto.request.LoginRequest;
import com.example.loudhotel.dto.request.RegisterRequest;
import com.example.loudhotel.dto.response.JwtResponse;

public interface AuthService {

    JwtResponse login(LoginRequest request);

    void register(RegisterRequest request);

    JwtResponse refreshToken(String refreshToken);

}
