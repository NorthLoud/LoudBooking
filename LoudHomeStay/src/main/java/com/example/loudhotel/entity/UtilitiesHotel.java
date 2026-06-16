package com.example.loudhotel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utilities_hotel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilitiesHotel {

    @EmbeddedId
    private UtilitiesHotelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hotelId")
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utilitiesId")
    @JoinColumn(name = "utilities_id")
    private Utilities utilities;
}
