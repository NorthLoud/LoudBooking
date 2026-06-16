package com.example.loudhotel.service;

import com.example.loudhotel.dto.request.UserRequest;
import com.example.loudhotel.dto.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long userId);

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long userId, UserRequest request);

    void deleteUser(Long userId);

    Page<UserResponse> getUsersPage(int page, int size);

    Page<UserResponse> searchUsers(String keyword, int page, int size, String sortBy, String sortDir);

    UserResponse getMyProfile();

    UserResponse updateMyProfile(UserRequest request);
}
