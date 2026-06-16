package com.example.loudhotel.controller;

import com.example.loudhotel.dto.request.UserRequest;
import com.example.loudhotel.dto.response.UserResponse;
import com.example.loudhotel.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMyProfile() {
        return userService.getMyProfile();
    }

    @PutMapping("/me")
    public UserResponse updateMyProfile(
            @Valid @RequestBody UserRequest request
    ) {
        return userService.updateMyProfile(request);
    }
}