package com.example.loudhotel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    private String imageUrl;
}