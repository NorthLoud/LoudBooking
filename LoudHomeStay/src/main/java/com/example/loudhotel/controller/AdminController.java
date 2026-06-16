package com.example.loudhotel.controller;

import com.example.loudhotel.entity.User;
import com.example.loudhotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // 👉 rất quan trọng
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository; // ✅ inject ở đây

    @GetMapping("/admin/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getManagers() {
        return userRepository.findByRoleAndIsDeletedFalse(User.Role.MANAGER);
    }
}