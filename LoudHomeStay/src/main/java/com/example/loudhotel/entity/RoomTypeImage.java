package com.example.loudhotel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_type_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private String imageUrl;

    private Boolean isMain;

    private LocalDateTime createdAt;
}