package com.example.loudhotel.service.impl;

import com.example.loudhotel.entity.*;
import com.example.loudhotel.exception.BadRequestException;
import com.example.loudhotel.repository.BillRepository;
import com.example.loudhotel.repository.RoomAssignmentRepository;
import com.example.loudhotel.service.RoomAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAssignmentServiceImpl implements RoomAssignmentService {

    private final RoomAssignmentRepository repository;
    private final BillRepository billRepository;

    @Override
    public List<RoomAssignment> getByBill(Bill bill) {
        return repository.findByBillDetail_Bill(bill);
    }

    @Override
    public void assignRoom(BillDetail billDetail, Room room) {

        // ❌ 1. Check đã gán chưa
        if (repository.existsByBillDetail(billDetail)) {
            throw new BadRequestException("BillDetail này đã được gán phòng");
        }

        // ❌ 2. Check phòng đang được sử dụng (QUAN TRỌNG)
        boolean isUsing = billRepository.existsRoomBeingUsed(
                room.getRoomId(),
                billDetail.getBill().getBillId()
        );

        if (isUsing) {
            throw new BadRequestException(
                    "Phòng " + room.getRoomNumber() + " đang được sử dụng"
            );
        }

        // ❌ 3. Check phòng bị trùng lịch
        boolean isOverlap = billRepository.existsActiveBooking(
                room.getRoomId(),
                billDetail.getBill().getBillId(),
                billDetail.getBill().getCheckInDate(),
                billDetail.getBill().getCheckOutDate()
        );

        if (isOverlap) {
            throw new BadRequestException(
                    "Phòng " + room.getRoomNumber() + " đã được đặt trong thời gian này"
            );
        }

        // ✅ OK thì gán
        RoomAssignment ra = new RoomAssignment();
        ra.setBillDetail(billDetail);
        ra.setRoom(room);
        ra.setAssignedAt(LocalDateTime.now());

        repository.save(ra);
    }

    @Override
    public boolean existsByBillDetail(BillDetail billDetail) {
        return repository.existsByBillDetail(billDetail);
    }

    @Override
    public void deleteByBill(Bill bill) {
        List<RoomAssignment> list = repository.findByBillDetail_Bill(bill);
        repository.deleteAll(list);
    }
}