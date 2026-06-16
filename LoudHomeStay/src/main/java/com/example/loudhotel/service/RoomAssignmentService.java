package com.example.loudhotel.service;

import com.example.loudhotel.entity.Bill;
import com.example.loudhotel.entity.BillDetail;
import com.example.loudhotel.entity.Room;
import com.example.loudhotel.entity.RoomAssignment;

import java.util.List;

public interface RoomAssignmentService {

    List<RoomAssignment> getByBill(Bill bill);

    void assignRoom(BillDetail billDetail, Room room);

    boolean existsByBillDetail(BillDetail billDetail);

    void deleteByBill(Bill bill);
}