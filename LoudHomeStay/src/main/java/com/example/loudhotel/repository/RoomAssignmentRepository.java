package com.example.loudhotel.repository;

import com.example.loudhotel.entity.Bill;
import com.example.loudhotel.entity.BillDetail;
import com.example.loudhotel.entity.RoomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomAssignmentRepository extends JpaRepository<RoomAssignment, Long> {
    List<RoomAssignment> findByBillDetail_Bill(Bill bill);

    boolean existsByBillDetail(BillDetail billDetail);
}