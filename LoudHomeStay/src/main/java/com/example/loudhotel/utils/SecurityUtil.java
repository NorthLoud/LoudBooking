package com.example.loudhotel.utils;

import com.example.loudhotel.config.CustomUserDetails;
import com.example.loudhotel.exception.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Long getCurrentUserId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new BadRequestException("Chưa đăng nhập");
        }

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return user.getUserId();
    }

    public static boolean hasRole(String role) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) return false;

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

}