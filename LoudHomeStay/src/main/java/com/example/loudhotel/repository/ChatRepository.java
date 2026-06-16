package com.example.loudhotel.repository;

import com.example.loudhotel.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c WHERE (c.sender.userId = :user1 AND c.recipient.userId = :user2) " +
           "OR (c.sender.userId = :user2 AND c.recipient.userId = :user1) " +
           "ORDER BY c.createdAt ASC")
    List<Chat> findChatBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT DISTINCT c.recipient.userId FROM Chat c WHERE c.sender.userId = :userId " +
           "UNION " +
           "SELECT DISTINCT c.sender.userId FROM Chat c WHERE c.recipient.userId = :userId")
    List<Long> findChatPartnerIds(@Param("userId") Long userId);

    List<Chat> findByRecipient_UserIdAndIsReadFalse(Long recipientId);
}
