package com.example.loudhotel.test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== BẮT ĐẦU ĐỒNG BỘ CẤU TRÚC CƠ SỞ DỮ LIỆU ===");
        try {
            // 1. Đồng bộ bảng users: thêm ACTIVE, update AVAILABLE -> ACTIVE, xóa AVAILABLE
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN status ENUM('AVAILABLE', 'BLOCKED', 'ACTIVE') NOT NULL DEFAULT 'ACTIVE'");
            jdbcTemplate.execute("UPDATE users SET status = 'ACTIVE' WHERE status = 'AVAILABLE'");
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN status ENUM('ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE'");
            log.info("Đồng bộ bảng users thành công!");

            // 2. Đồng bộ bảng hotels: thêm ACTIVE, update AVAILABLE -> ACTIVE, xóa AVAILABLE
            jdbcTemplate.execute("ALTER TABLE hotels MODIFY COLUMN hotel_status ENUM('AVAILABLE', 'INACTIVE', 'MAINTENANCE', 'ACTIVE') NOT NULL DEFAULT 'ACTIVE'");
            jdbcTemplate.execute("UPDATE hotels SET hotel_status = 'ACTIVE' WHERE hotel_status = 'AVAILABLE'");
            jdbcTemplate.execute("ALTER TABLE hotels MODIFY COLUMN hotel_status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') NOT NULL DEFAULT 'ACTIVE'");
            log.info("Đồng bộ bảng hotels thành công!");

            // 3. Đồng bộ bảng rooms: chuyển ACTIVE sang AVAILABLE và định nghĩa lại ENUM
            jdbcTemplate.execute("UPDATE rooms SET room_status = 'AVAILABLE' WHERE room_status = 'ACTIVE'");
            jdbcTemplate.execute("ALTER TABLE rooms MODIFY COLUMN room_status ENUM('AVAILABLE', 'MAINTENANCE', 'OCCUPIED', 'INACTIVE') NOT NULL DEFAULT 'AVAILABLE'");
            log.info("Đồng bộ bảng rooms thành công!");
            
            log.info("=== ĐỒNG BỘ CẤU TRÚC CƠ SỞ DỮ LIỆU HOÀN THÀNH XUẤT SẮC ===");
        } catch (Exception e) {
            log.error("Lỗi trong quá trình đồng bộ cơ sở dữ liệu: ", e);
        }
    }
}
