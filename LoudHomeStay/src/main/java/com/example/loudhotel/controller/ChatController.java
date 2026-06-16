package com.example.loudhotel.controller;

import com.example.loudhotel.dto.request.ChatRequest;
import com.example.loudhotel.dto.response.ChatResponse;
import com.example.loudhotel.dto.response.ChatUserResponse;
import com.example.loudhotel.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send")
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        ChatResponse res = chatService.sendMessage(request);
        messagingTemplate.convertAndSend("/topic/chat/" + res.getSenderId(), res);
        messagingTemplate.convertAndSend("/topic/chat/" + res.getRecipientId(), res);
        messagingTemplate.convertAndSend("/topic/user/" + res.getSenderId(), res);
        messagingTemplate.convertAndSend("/topic/user/" + res.getRecipientId(), res);
        return res;
    }

    @GetMapping("/messages/{partnerId}")
    public List<ChatResponse> getMessages(@PathVariable Long partnerId) {
        return chatService.getMessages(partnerId);
    }

    @GetMapping("/partners")
    public List<ChatUserResponse> getChatPartners() {
        return chatService.getChatPartners();
    }

    @PostMapping("/upload")
    public List<String> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        return chatService.uploadChatImages(files);
    }

    @PostMapping("/read/{partnerId}")
    public void markAsRead(@PathVariable Long partnerId) {
        chatService.markAsRead(partnerId);
    }

    @PutMapping("/edit/{chatId}")
    public ChatResponse editMessage(@PathVariable Long chatId, @RequestBody String content) {
        ChatResponse res = chatService.editMessage(chatId, content);
        messagingTemplate.convertAndSend("/topic/chat/" + res.getSenderId(), res);
        messagingTemplate.convertAndSend("/topic/chat/" + res.getRecipientId(), res);
        return res;
    }

    @DeleteMapping("/delete/{chatId}")
    public void deleteMessage(@PathVariable Long chatId) {
        ChatResponse res = chatService.getChatById(chatId);
        chatService.deleteMessage(chatId);
        messagingTemplate.convertAndSend("/topic/chat/" + res.getSenderId(), res);
        messagingTemplate.convertAndSend("/topic/chat/" + res.getRecipientId(), res);
    }

    @GetMapping("/partner/{id}")
    public ChatUserResponse getPartnerInfo(@PathVariable Long id) {
        return chatService.getPartnerInfo(id);
    }
}