package com.example.loudhotel.service.impl;

import com.example.loudhotel.config.JwtUtil;
import com.example.loudhotel.dto.request.LoginRequest;
import com.example.loudhotel.dto.request.RegisterRequest;
import com.example.loudhotel.dto.response.JwtResponse;
import com.example.loudhotel.entity.User;
import com.example.loudhotel.exception.BadRequestException;
import com.example.loudhotel.repository.UserRepository;
import com.example.loudhotel.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public JwtResponse login(LoginRequest request) {

        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Sai email hoặc mật khẩu"));

        if (user.getStatus() == User.Status.BLOCKED) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Sai email hoặc mật khẩu");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // ✅ LƯU refresh token vào DB
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return JwtResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void register(RegisterRequest request) {

        // ===== CHECK USERNAME =====
        userRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
                .ifPresent(u -> {
                    if (u.getStatus() == User.Status.BLOCKED) {
                        throw new BadRequestException("Tài khoản đã bị khóa, vui lòng liên hệ admin!");
                    }
                    throw new BadRequestException("Username đã tồn tại");
                });

        // ===== CHECK EMAIL =====
        userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .ifPresent(u -> {
                    if (u.getStatus() == User.Status.BLOCKED) {
                        throw new BadRequestException("Email này đã bị khóa!");
                    }
                    throw new BadRequestException("Email đã tồn tại");
                });

        // ===== CHECK PHONE =====
        userRepository.findByPhoneAndIsDeletedFalse(request.getPhone())
                .ifPresent(u -> {
                    if (u.getStatus() == User.Status.BLOCKED) {
                        throw new BadRequestException("SĐT này đã bị khóa!");
                    }
                    throw new BadRequestException("Số điện thoại đã tồn tại");
                });

        // ===== CREATE USER =====
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .status(User.Status.ACTIVE)
                .build();

        userRepository.save(user);
    }

    @Override
    public JwtResponse refreshToken(String refreshToken) {

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BadRequestException("Refresh token không hợp lệ");
        }

        String email = jwtUtil.getEmail(refreshToken);

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException("User không tồn tại"));

        // ✅ KIỂM TRA refresh token có khớp DB không
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new BadRequestException("Refresh token không đúng hoặc đã bị thu hồi");
        }

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name()
        );

        return JwtResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

}
