package com.example.loudhotel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hotelId;

    @Column(nullable = false)
    private String hotelName;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    private Double averageRating;

    @Transient
    private Integer roomsTotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HotelStatus hotelStatus = HotelStatus.ACTIVE;

    public enum HotelStatus {
        ACTIVE,      // 0
        INACTIVE,    // 1
        MAINTENANCE     // 2
    }

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    private List<HotelImage> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @OneToMany(mappedBy = "hotel")
    private List<RoomType> roomTypes;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    private List<UtilitiesHotel> utilitiesHotels;
}
