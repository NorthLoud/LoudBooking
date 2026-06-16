package com.example.loudhotel.service;

import com.example.loudhotel.dto.request.VoucherRequest;
import com.example.loudhotel.dto.response.VoucherResponse;

import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getAllVouchers();

    VoucherResponse getVoucherById(Long id);

    VoucherResponse createVoucher(VoucherRequest request);

    VoucherResponse updateVoucher(Long id, VoucherRequest request);

    void deleteVoucher(Long id);

    List<VoucherResponse> getPublicVouchers(Long hotelId);

    List<VoucherResponse> getManagerVouchers();
}
