package com.example.loudhotel.service;

import com.example.loudhotel.dto.request.BillRequest;
import com.example.loudhotel.dto.response.BillResponse;

import java.util.List;

public interface BillService {

    BillResponse create(Long userId, BillRequest request);

    BillResponse getById(Long billId);

    BillResponse cancel(Long billId);

    BillResponse assignRooms(Long billId, com.example.loudhotel.dto.request.CheckInRequest request);
    

    BillResponse checkIn(Long billId, com.example.loudhotel.dto.request.CheckInRequest request);

    BillResponse addExtraFee(Long billId, com.example.loudhotel.dto.request.ExtraFeeRequest request);

    BillResponse checkOut(Long billId);

    List<BillResponse> getAll();

    List<BillResponse> getByUser(Long userId);
    
    BillResponse pay(Long billId);
    
    BillResponse payExtraFee(Long billId, Long extraFeeId);

    List<BillResponse> getBillsOfManager(Long managerId);

    BillResponse update(Long billId, BillRequest request);
}
