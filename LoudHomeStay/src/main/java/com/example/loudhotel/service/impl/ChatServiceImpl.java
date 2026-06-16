package com.example.loudhotel.service.impl;

import com.example.loudhotel.config.FileStorageConfig;
import com.example.loudhotel.dto.request.ChatRequest;
import com.example.loudhotel.dto.response.ChatResponse;
import com.example.loudhotel.dto.response.ChatUserResponse;
import com.example.loudhotel.entity.*;
import com.example.loudhotel.exception.ResourceNotFoundException;
import com.example.loudhotel.repository.*;
import com.example.loudhotel.service.ChatService;
import com.example.loudhotel.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatImageRepository chatImageRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        Long senderId = SecurityUtil.getCurrentUserId();
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User recipient = null;
        Hotel hotel = null;

        if (request.getHotelId() != null) {
            hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

            // If sender is a USER, recipient is the hotel manager
            if (sender.getRole() == User.Role.USER) {
                recipient = hotel.getManager();
            } else {
                // If sender is MANAGER/ADMIN, recipient must be provided
                if (request.getRecipientId() == null) {
                    throw new RuntimeException("RecipientId is required when sending as manager");
                }
                recipient = userRepository.findById(request.getRecipientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
            }
        } else {
            recipient = userRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
        }

        Chat replyTo = null;
        if (request.getReplyToId() != null) {
            replyTo = chatRepository.findById(request.getReplyToId()).orElse(null);
        }

        Chat chat = Chat.builder()
                .sender(sender)
                .recipient(recipient)
                .hotel(hotel)
                .contentText(request.getContent())
                .isRead(false)
                .isEdited(false)
                .isDeleted(false)
                .replyTo(replyTo)
                .build();

        chat = chatRepository.save(chat);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (String url : request.getImages()) {
                ChatImage image = ChatImage.builder()
                        .chat(chat)
                        .imageUrl(url)
                        .build();
                chatImageRepository.save(image);
            }
        }

        return mapToResponse(chat, request.getImages());
    }

    @Override
    public List<ChatResponse> getMessages(Long partnerId) {
        // This is tricky now because we want messages contextually by hotel too.
        // But for simplicity, we'll fetch all between users.
        // Actually, let's keep it contextual if we want Messenger style.
        // If a partnerId is provided, we fetch all.
        Long userId = SecurityUtil.getCurrentUserId();
        List<Chat> chats = chatRepository.findChatBetweenUsers(userId, partnerId);

        return chats.stream()
                .map(chat -> {
                    List<String> images = chatImageRepository.findByChat_ChatId(chat.getChatId())
                            .stream()
                            .map(ChatImage::getImageUrl)
                            .collect(Collectors.toList());
                    return mapToResponse(chat, images);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatUserResponse> getChatPartners() {
        Long userId = SecurityUtil.getCurrentUserId();
        User currentUser = userRepository.findById(userId).orElseThrow();

        List<Chat> allMyChats = chatRepository.findAll().stream()
                .filter(c -> c.getSender().getUserId().equals(userId) || c.getRecipient().getUserId().equals(userId))
                .collect(Collectors.toList());

        // Group by partner and hotel
        Map<String, List<Chat>> grouped = allMyChats.stream()
                .collect(Collectors.groupingBy(c -> {
                    Long pId = c.getSender().getUserId().equals(userId) ? c.getRecipient().getUserId()
                            : c.getSender().getUserId();
                    Long hId = c.getHotel() != null ? c.getHotel().getHotelId() : 0L;
                    return pId + "_" + hId;
                }));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("_");
                    Long partnerId = Long.parseLong(parts[0]);
                    Long hotelId = Long.parseLong(parts[1]);
                    List<Chat> conversation = entry.getValue();

                    User partner = userRepository.findById(partnerId).orElse(null);
                    Hotel hotel = hotelId != 0L ? hotelRepository.findById(hotelId).orElse(null) : null;

                    Chat lastChat = conversation.stream().max(Comparator.comparing(Chat::getCreatedAt)).orElse(null);
                    int unreadCount = (int) conversation.stream()
                            .filter(c -> c.getRecipient().getUserId().equals(userId) && !c.getIsRead())
                            .count();

                    String firstName = partner.getFirstName() != null ? partner.getFirstName() : "";
                    String lastName = partner.getLastName() != null ? partner.getLastName() : "";
                    String displayName = (firstName + " " + lastName).trim();
                    if (displayName.isEmpty()) {
                        displayName = partner.getUsername();
                    }
                    String hotelImage = null;
                    if (hotel != null && hotel.getImages() != null && !hotel.getImages().isEmpty()) {
                        hotelImage = hotel.getImages().stream()
                                .filter(HotelImage::getIsMain)
                                .findFirst()
                                .map(HotelImage::getImageUrl)
                                .orElse(hotel.getImages().get(0).getImageUrl());

                        // Convert to full URL if needed (optional depending on frontend handling)
                        if (hotelImage != null && !hotelImage.startsWith("http") && !hotelImage.startsWith("/")) {
                            hotelImage = "/images/hotels/" + hotelImage;
                        }
                    }

                    if (currentUser.getRole() == User.Role.USER && hotel != null) {
                        displayName = hotel.getHotelName();
                    }

                    return ChatUserResponse.builder()
                            .userId(partner.getUserId())
                            .username(partner.getUsername())
                            .fullName(displayName)
                            .avatar(null) // User doesn't have avatar field yet
                            .hotelId(hotel != null ? hotel.getHotelId() : null)
                            .hotelName(hotel != null ? hotel.getHotelName() : null)
                            .hotelImage(hotelImage)
                            .lastMessage(lastChat != null ? lastChat.getContentText() : "")
                            .lastMessageAt(lastChat != null ? lastChat.getCreatedAt() : null)
                            .unreadCount(unreadCount)
                            .build();
                })
                .sorted(Comparator.comparing(ChatUserResponse::getLastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> uploadChatImages(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(FileStorageConfig.CHAT_PATH + fileName);
            try {
                Files.write(path, file.getBytes());
                urls.add("/images/chat/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("Upload failed", e);
            }
        }
        return urls;
    }

    @Override
    public void markAsRead(Long partnerId) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Chat> unreadChats = chatRepository.findByRecipient_UserIdAndIsReadFalse(userId);
        for (Chat chat : unreadChats) {
            if (chat.getSender().getUserId().equals(partnerId)) {
                chat.setIsRead(true);
                chatRepository.save(chat);
            }
        }
    }

    @Override
    public void deleteMessage(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        Long userId = SecurityUtil.getCurrentUserId();
        if (!chat.getSender().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        chat.setIsDeleted(true);
        chatRepository.save(chat);
    }

    @Override
    public ChatResponse editMessage(Long chatId, String newContent) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        Long userId = SecurityUtil.getCurrentUserId();
        if (!chat.getSender().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        chat.setContentText(newContent);
        chat.setIsEdited(true);
        chat = chatRepository.save(chat);
        List<String> images = chatImageRepository.findByChat_ChatId(chat.getChatId())
                .stream().map(ChatImage::getImageUrl).collect(Collectors.toList());
        return mapToResponse(chat, images);
    }

    @Override
    public ChatResponse getChatById(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        List<String> images = chatImageRepository.findByChat_ChatId(chat.getChatId())
                .stream().map(ChatImage::getImageUrl).collect(Collectors.toList());
        return mapToResponse(chat, images);
    }

    @Override
    public ChatUserResponse getPartnerInfo(Long partnerId) {

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Hotel hotel = hotelRepository.findByManager_UserId(partnerId)
                .orElse(null);

        String hotelImage = null;

        if (hotel != null && hotel.getImages() != null && !hotel.getImages().isEmpty()) {

            hotelImage = hotel.getImages().stream()
                    .filter(HotelImage::getIsMain)
                    .findFirst()
                    .map(HotelImage::getImageUrl)
                    .orElse(hotel.getImages().get(0).getImageUrl());

            if (hotelImage != null
                    && !hotelImage.startsWith("http")
                    && !hotelImage.startsWith("/")) {

                hotelImage = "/images/hotels/" + hotelImage;
            }
        }

        return ChatUserResponse.builder()
                .userId(partner.getUserId())
                .username(partner.getUsername())
                .fullName(hotel != null ? hotel.getHotelName() : formatName(partner))
                .hotelId(hotel != null ? hotel.getHotelId() : null)
                .hotelName(hotel != null ? hotel.getHotelName() : null)
                .hotelImage(hotelImage)
                .build();
    }
    private ChatResponse mapToResponse(Chat chat, List<String> images) {
        return ChatResponse.builder()
                .chatId(chat.getChatId())
                .senderId(chat.getSender().getUserId())
                .recipientId(chat.getRecipient().getUserId())
                .hotelId(chat.getHotel() != null ? chat.getHotel().getHotelId() : null)
                .content(chat.getContentText())
                .images(images)
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .isEdited(chat.getIsEdited())
                .isRead(chat.getIsRead())
                .isDeleted(chat.getIsDeleted())
                .replyToUser(chat.getReplyTo() != null ? formatName(chat.getReplyTo().getSender()) : null)
                .replyToContent(chat.getReplyTo() != null ? chat.getReplyTo().getContentText() : null)
                .replyToId(chat.getReplyTo() != null ? chat.getReplyTo().getChatId() : null)
                .build();
    }

    private String formatName(User user) {
        String f = (user.getFirstName() != null && !user.getFirstName().equalsIgnoreCase("null")) ? user.getFirstName()
                : "";
        String l = (user.getLastName() != null && !user.getLastName().equalsIgnoreCase("null")) ? user.getLastName()
                : "";
        String name = (f + " " + l).trim();
        return name.isEmpty() ? user.getUsername() : name;
    }

}