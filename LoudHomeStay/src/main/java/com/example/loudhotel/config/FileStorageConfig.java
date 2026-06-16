package com.example.loudhotel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    public static final String BASE_PATH = "src/main/resources/static/images_booking/";
    public static final String ROOM_PATH = BASE_PATH;
    public static final String HOTEL_PATH = BASE_PATH;
    public static final String CHAT_PATH = BASE_PATH;
    public static final String ROOM_TYPE_PATH = BASE_PATH;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cung cấp ảnh từ thư mục static/images_booking thông qua các URL /images/...
        registry.addResourceHandler("/images/rooms/**")
                .addResourceLocations("file:" + ROOM_PATH);

        registry.addResourceHandler("/images/hotels/**")
                .addResourceLocations("file:" + HOTEL_PATH);

        registry.addResourceHandler("/images/chat/**")
                .addResourceLocations("file:" + CHAT_PATH);

        registry.addResourceHandler("/images/room-types/**")
                .addResourceLocations("file:" + ROOM_TYPE_PATH);
    }
}
