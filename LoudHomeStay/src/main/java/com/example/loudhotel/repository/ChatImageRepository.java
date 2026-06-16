package com.example.loudhotel.repository;

import com.example.loudhotel.entity.ChatImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatImageRepository extends JpaRepository<ChatImage, Long> {
    List<ChatImage> findByChat_ChatId(Long chatId);
}