package com.example.loudhotel.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VnPayConfig {

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String payUrl;

    @Value("${vnpay.returnUrl.user}")
    private String returnUrlUser;

    public String getReturnUrlUser() {
        return returnUrlUser;
    }
    public String getTmnCode() {
        return tmnCode;
    }
    public String getHashSecret() {
        return hashSecret;
    }
    public String getPayUrl() {
        return payUrl;
    }
    public static String getIpAddress(HttpServletRequest request) {

        String ipAddress = request.getHeader("X-FORWARDED-FOR");

        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }
}