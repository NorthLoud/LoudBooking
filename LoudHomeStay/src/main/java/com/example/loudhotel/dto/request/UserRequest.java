package com.example.loudhotel.dto.request;

import com.example.loudhotel.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Username không được bỏ trống")
    private String username;

    @NotBlank(message = "Email không được bỏ trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được bỏ trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải gồm 10 chữ số")
    private String phone;

    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Tên không được bỏ trống")
    private String firstName;

    @NotBlank(message = "Họ không được bỏ trống")
    private String lastName;

    private User.Role role;

    private User.Status status;
}
