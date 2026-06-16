package com.example.loudhotel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utilities_room_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder   // 🔥 THÊM DÒNG NÀY
public class UtilitiesRoomType {

    @EmbeddedId
    private UtilitiesRoomTypeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("typeId")
    @JoinColumn(name = "type_id")
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utilitiesId")
    @JoinColumn(name = "utilities_id")
    private Utilities utilities;
}