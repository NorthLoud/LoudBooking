package com.example.loudhotel.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomTypeUtilitySummaryResponse {

    private Long typeId;
    private String typeName;
    private Integer utilityCount;
    private String hotelName;
    private Long hotelId;

    public RoomTypeUtilitySummaryResponse(Long typeId, String typeName, Long utilityCount, String hotelName, Long hotelId) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.utilityCount = utilityCount != null ? utilityCount.intValue() : 0;
        this.hotelName = hotelName;
        this.hotelId = hotelId;
    }
}