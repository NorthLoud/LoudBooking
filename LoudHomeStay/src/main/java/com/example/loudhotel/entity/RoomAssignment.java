package com.example.loudhotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bill_detail_id")
    private BillDetail billDetail;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDateTime assignedAt;

}