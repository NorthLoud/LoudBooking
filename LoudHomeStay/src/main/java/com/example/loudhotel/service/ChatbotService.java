package com.example.loudhotel.service;

import com.example.loudhotel.dto.request.ChatbotRequest;
import com.example.loudhotel.dto.response.ChatbotResponse;

public interface ChatbotService {
    ChatbotResponse chat(ChatbotRequest request);
}
