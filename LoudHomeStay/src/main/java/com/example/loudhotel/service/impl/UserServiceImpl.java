package com.example.loudhotel.service.impl;

import com.example.loudhotel.config.CustomUserDetails;
import com.example.loudhotel.dto.request.UserRequest;
import com.example.loudhotel.dto.response.UserResponse;
import com.example.loudhotel.entity.User;
import com.example.loudhotel.exception.BadRequestException;
import com.example.loudhotel.exception.ResourceNotFoundException;
import com.example.loudhotel.repository.HotelRepository;
import com.example.loudhotel.repository.UserRepository;
import com.example.loudhotel.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HotelRepository hotelRepository;

    private UserResponse mapToResponse(User u) {
        return UserResponse.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .email(u.getEmail())
                .phone(u.getPhone())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .fullName(
                        (u.getLastName() == null ? "" : u.getLastName()) +
                                " " +
                                (u.getFirstName() == null ? "" : u.getFirstName())
                )
                .role(u.getRole())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != User.Role.ADMIN) // loại admin
                .filter(u -> !u.getIsDeleted())              // bỏ user đã xoá
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        return mapToResponse(user);
    }

    @Override
    public UserResponse createUser(UserRequest req) {


        if (userRepository.existsByUsernameAndIsDeletedFalse(req.getUsername()))
            throw new BadRequestException("Username đã tồn tại!");

        if (userRepository.existsByEmailAndIsDeletedFalse(req.getEmail()))
            throw new BadRequestException("Email đã tồn tại!");

        if (userRepository.existsByPhoneAndIsDeletedFalse(req.getPhone()))
            throw new BadRequestException("Số điện thoại đã tồn tại!");

        if (req.getPassword() == null || req.getPassword().isBlank())
            throw new BadRequestException("Password không được bỏ trống!");

        if (req.getRole() == User.Role.ADMIN) {
            throw new BadRequestException("Không được tạo ADMIN!");
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .phone(req.getPhone())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : User.Role.USER)
                .status(User.Status.ACTIVE)
                .build();

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest req) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại!"));

        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }

        // Cho đổi role
        if (req.getRole() != null) {
            if (req.getRole() == User.Role.ADMIN) {
                throw new BadRequestException("Không được cấp quyền ADMIN!");
            }
            user.setRole(req.getRole());
        }
        if (user.getRole() == User.Role.ADMIN)
            throw new BadRequestException("Không được sửa ADMIN!");

        // Check trùng username (trừ chính nó)
        if (!user.getUsername().equals(req.getUsername())
                && userRepository.existsByUsernameAndIsDeletedFalse(req.getUsername())) {
            throw new BadRequestException("Username đã tồn tại!");
        }

        // Check trùng email
        if (!user.getEmail().equals(req.getEmail())
                && userRepository.existsByEmailAndIsDeletedFalse(req.getEmail())) {
            throw new BadRequestException("Email đã tồn tại!");
        }

        // Check trùng phone
        if (!user.getPhone().equals(req.getPhone())
                && userRepository.existsByPhoneAndIsDeletedFalse(req.getPhone())) {
            throw new BadRequestException("Số điện thoại đã tồn tại!");
        }

        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());

        // Nếu có password thì mới update
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại!"));

        if (user.getRole() == User.Role.ADMIN)
            throw new RuntimeException("Không được xoá ADMIN!");

        // ❌ CHECK MANAGER đang quản lý hotel
        if (user.getRole() == User.Role.MANAGER) {

            boolean hasHotel = hotelRepository
                    .existsByManager_UserIdAndIsDeletedFalse(user.getUserId());

            if (hasHotel) {
                throw new RuntimeException(
                        "Manager đang quản lý khách sạn. Hãy chuyển khách sạn trước khi xoá!"
                );
            }
        }

        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public Page<UserResponse> getUsersPage(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findByRoleNotAndIsDeletedFalse(User.Role.ADMIN, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword == null || keyword.isBlank()) {
            return userRepository.findByRoleNotAndIsDeletedFalse(User.Role.ADMIN, pageable)
                    .map(this::mapToResponse);
        }

        return userRepository.searchUsers(User.Role.ADMIN, keyword, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public UserResponse getMyProfile() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) auth.getPrincipal();

        User user = userRepository
                .findById(userDetails.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User không tồn tại"));

        return mapToResponse(user);
    }

    @Override
    public UserResponse updateMyProfile(UserRequest req) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) auth.getPrincipal();

        User user = userRepository
                .findById(userDetails.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User không tồn tại"));


        // check email
        if (!user.getEmail().equals(req.getEmail())
                && userRepository.existsByEmailAndIsDeletedFalse(req.getEmail())) {

            throw new BadRequestException("Email đã tồn tại");
        }

        // check phone
        if (!user.getPhone().equals(req.getPhone())
                && userRepository.existsByPhoneAndIsDeletedFalse(req.getPhone())) {

            throw new BadRequestException("Số điện thoại đã tồn tại");
        }

        user.setUsername(req.getUsername());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());

        // đổi mật khẩu nếu nhập
        if (req.getPassword() != null
                && !req.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(req.getPassword())
            );
        }

        return mapToResponse(
                userRepository.save(user)
        );
    }

}
