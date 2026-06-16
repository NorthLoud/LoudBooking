package com.example.loudhotel.service;

import com.example.loudhotel.dto.request.ChatRequest;
import com.example.loudhotel.dto.response.ChatResponse;
import com.example.loudhotel.dto.response.ChatUserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {
    ChatResponse sendMessage(ChatRequest request);
    List<ChatResponse> getMessages(Long partnerId);
    List<ChatUserResponse> getChatPartners();
    List<String> uploadChatImages(List<MultipartFile> files);
    void markAsRead(Long partnerId);
    void deleteMessage(Long chatId);
    ChatResponse editMessage(Long chatId, String newContent);
    ChatResponse getChatById(Long chatId);
    ChatUserResponse getPartnerInfo(Long partnerId);
}