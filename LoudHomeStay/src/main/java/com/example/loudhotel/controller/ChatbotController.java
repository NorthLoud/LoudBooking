package com.example.loudhotel.controller;

import com.example.loudhotel.dto.request.ChatbotRequest;
import com.example.loudhotel.dto.response.ChatbotResponse;
import com.example.loudhotel.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<ChatbotResponse> chat(@RequestBody ChatbotRequest request) {
        log.info("Nhận request chat: {}", request.getMessage());
        ChatbotResponse response = chatbotService.chat(request);
        return ResponseEntity.ok(response);
    }
}
