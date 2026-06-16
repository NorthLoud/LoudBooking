package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.request.BillRequest;
import com.example.loudhotel.dto.response.BillResponse;
import com.example.loudhotel.entity.*;
import com.example.loudhotel.exception.BadRequestException;
import com.example.loudhotel.exception.ResourceNotFoundException;
import com.example.loudhotel.repository.*;
import com.example.loudhotel.service.BillService;
import com.example.loudhotel.service.RoomAssignmentService;
import com.example.loudhotel.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Transactional
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomAssignmentService roomAssignmentService;
    private final BillExtraFeeRepository billExtraFeeRepository;
    private final VoucherRepository voucherRepository;
    private final ReviewRepository reviewRepository;
    private final com.example.loudhotel.service.SseService sseService;

    private BillResponse toResponse(Bill bill) {

        var assignments = roomAssignmentService.getByBill(bill);

        List<BillResponse.RoomItem> rooms = assignments.stream()
                .map(a -> {
                    Double price = a.getBillDetail().getPriceAtBooking() != null ? a.getBillDetail().getPriceAtBooking()
                            : a.getBillDetail().getRoomType().getPrice();
                    return BillResponse.RoomItem.builder()
                            .roomId(a.getRoom().getRoomId())
                            .roomNumber(a.getRoom().getRoomNumber())
                            .roomType(a.getBillDetail().getRoomType().getTypeName())
                            .capacity(a.getBillDetail().getRoomType().getCapacity())
                            .nights(a.getBillDetail().getNights())
                            .guestCount(a.getBillDetail().getGuestCount())
                            .price(price)
                            .subtotal(price * a.getBillDetail().getNights())
                            .build();
                }).toList();

        List<BillResponse.BillDetailItem> details = bill.getBillDetails().stream()
                .map(d -> {
                    Double price = d.getPriceAtBooking() != null ? d.getPriceAtBooking() : d.getRoomType().getPrice();
                    return BillResponse.BillDetailItem.builder()
                            .typeId(d.getRoomType().getTypeId())
                            .typeName(d.getRoomType().getTypeName())
                            .quantity(1)
                            .priceAtBooking(price)
                            .nights(d.getNights())
                            .guestCount(d.getGuestCount())
                            .build();
                }).toList();

        List<BillResponse.ExtraFeeItem> extraFees = new ArrayList<>();
        if (bill.getExtraFees() != null) {
            extraFees = bill.getExtraFees().stream()
                    .map(f -> {
                        BillResponse.ExtraFeeItem item = BillResponse.ExtraFeeItem.builder()
                                .extraFeeId(f.getExtraFeeId())
                                .amount(f.getAmount())
                                .reason(f.getReason())
                                .createdAt(f.getCreatedAt())
                                .isPaid(f.isPaid())
                                .build();
                        return item;
                    })
                    .toList();
        }

        double roomTotal = bill.getBillDetails().stream()
                .mapToDouble(d -> {
                    Double price = d.getPriceAtBooking() != null
                            ? d.getPriceAtBooking()
                            : d.getRoomType().getPrice();

                    int nights = d.getNights() != null ? d.getNights() : 0;

                    return price * nights;
                })
                .sum();

        double extraFeeTotal = extraFees.stream()
                .mapToDouble(f -> f.getAmount() != null ? f.getAmount() : 0)
                .sum();

        return BillResponse.builder()
                .billId(bill.getBillId())
                .billCode(bill.getBillCode())
                .userId(bill.getUser().getUserId())
                .userName(bill.getUser().getUsername())
                .hotelId(bill.getHotel().getHotelId())
                .hotelName(bill.getHotel().getHotelName())
                .hotelAddress(bill.getHotel().getAddress())
                .managerEmail(bill.getHotel().getManager().getEmail())
                .orderName(bill.getOrderName())
                .orderEmail(bill.getOrderEmail())
                .orderPhone(bill.getOrderPhone())
                .checkInDate(bill.getCheckInDate())
                .checkOutDate(bill.getCheckOutDate())
                .roomTotal(roomTotal)
                .extraFeeTotal(extraFeeTotal)
                .discountAmount(bill.getDiscountAmount())
                .voucherCode(bill.getVoucher() != null ? bill.getVoucher().getVoucherCode() : null)
                .totalCost(bill.getTotalCost())
                .billStatus(bill.getBillStatus())
                .paymentMethod(bill.getPaymentMethod())
                .cancelReason(bill.getCancelReason())
                .idCardCode(bill.getIdCardCode())
                .guestCount(bill.getBillDetails().stream()
                        .mapToInt(d -> d.getGuestCount() != null ? d.getGuestCount() : 0).sum())
                .actualCheckInTime(bill.getActualCheckInTime())
                .actualCheckOutTime(bill.getActualCheckOutTime())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .rooms(rooms)
                .details(details)
                .extraFees(extraFees)
                .isReviewed(reviewRepository.existsByBill_BillId(bill.getBillId()))
                .build();
    }

    private void recalculateAndSaveTotalCost(Bill bill) {
        double roomTotal = bill.getBillDetails().stream()
                .mapToDouble(d -> {
                    double price = d.getPriceAtBooking() != null ? d.getPriceAtBooking() : d.getRoomType().getPrice();
                    int nights = d.getNights() != null ? d.getNights() : 0;
                    return price * nights;
                })
                .sum();

        double extraFeeTotal = 0;
        if (bill.getExtraFees() != null) {
            extraFeeTotal = bill.getExtraFees().stream()
                    .mapToDouble(f -> f.getAmount() != null ? f.getAmount() : 0)
                    .sum();
        }

        double discount = bill.getDiscountAmount() != null ? bill.getDiscountAmount() : 0;
        bill.setTotalCost(roomTotal + extraFeeTotal - discount);
        billRepository.save(bill);
    }

    @Override
    public List<BillResponse> getAll() {
        return billRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<BillResponse> getBillsOfManager(Long managerId) {

        return billRepository
                .findByHotel_Manager_UserId(managerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BillResponse create(Long userId, BillRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel không tồn tại"));

        long nights = ChronoUnit.DAYS.between(
                request.getCheckInDate(),
                request.getCheckOutDate());

        if (nights <= 0) {
            throw new BadRequestException("Check-out phải sau check-in");
        }

        List<BillDetail> details = new ArrayList<>();
        double totalCost = 0;

        java.util.Map<Long, Integer> typeToRequestedQty = new java.util.HashMap<>();
        for (BillRequest.RoomSelection r : request.getRooms()) {
            typeToRequestedQty.put(r.getTypeId(), typeToRequestedQty.getOrDefault(r.getTypeId(), 0) + r.getQuantity());
        }

        for (BillRequest.RoomSelection r : request.getRooms()) {

            RoomType roomType = roomTypeRepository.findById(r.getTypeId())
                    .orElseThrow(() -> new RuntimeException("Room type not found"));

            // check đủ phòng
            int totalRooms = roomRepository.countTotalRoomsForBooking(roomType.getTypeId());

            int booked = roomRepository.countBookedRooms(
                    roomType.getTypeId(),
                    request.getCheckInDate(),
                    request.getCheckOutDate());

            int available = totalRooms - booked;

            if (available < typeToRequestedQty.get(r.getTypeId())) {
                throw new BadRequestException("Không đủ phòng trống");
            }

            double price = roomType.getPrice();

            for (int i = 0; i < r.getQuantity(); i++) {
                BillDetail bd = BillDetail.builder()
                        .roomType(roomType)
                        .priceAtBooking(price)
                        .nights((int) nights)
                        .guestCount(r.getGuestCount())
                        .build();

                details.add(bd);
                totalCost += price * nights;
            }
        }

        double roomTotal = totalCost;
        Double discountAmount = 0.0;
        Voucher appliedVoucher = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            Voucher voucher = voucherRepository.findByVoucherCodeAndIsDeletedFalse(request.getVoucherCode())
                    .orElseThrow(() -> new BadRequestException("Mã giảm giá không hợp lệ"));

            LocalDateTime now = LocalDateTime.now();
            if (voucher.getStatus() != Voucher.VoucherStatus.ACTIVE ||
                    (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) ||
                    (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate()))) {
                throw new BadRequestException("Mã giảm giá không khả dụng hoặc đã hết hạn");
            }

            if (voucher.getQuantity() != null && voucher.getUsedCount() >= voucher.getQuantity()) {
                throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng");
            }

            if (voucher.getMinBillAmount() != null && roomTotal < voucher.getMinBillAmount()) {
                throw new BadRequestException("Chưa đạt giá trị đơn hàng tối thiểu để áp dụng mã này");
            }

            boolean isHotelEligible = voucher.getHotels() == null || voucher.getHotels().isEmpty() ||
                    voucher.getHotels().stream().anyMatch(h -> h.getHotelId().equals(hotel.getHotelId()));
            if (!isHotelEligible) {
                throw new BadRequestException("Mã giảm giá không áp dụng cho khách sạn này");
            }

            if (voucher.getDiscountType() == Voucher.DiscountType.PERCENT) {
                discountAmount = roomTotal * voucher.getDiscountValue() / 100.0;
                if (voucher.getMaxDiscountAmount() != null && discountAmount > voucher.getMaxDiscountAmount()) {
                    discountAmount = voucher.getMaxDiscountAmount();
                }
            } else if (voucher.getDiscountType() == Voucher.DiscountType.FIXED) {
                discountAmount = voucher.getDiscountValue();
            }

            if (discountAmount > roomTotal) {
                discountAmount = roomTotal;
            }

            totalCost = roomTotal - discountAmount;

            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
            appliedVoucher = voucher;
        }

        Bill bill = Bill.builder()
                .user(user)
                .hotel(hotel)
                .billCode(generateBillCode())
                .orderName(request.getOrderName())
                .orderEmail(request.getOrderEmail())
                .orderPhone(request.getOrderPhone())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .totalCost(totalCost)
                .voucher(appliedVoucher)
                .discountAmount(discountAmount > 0 ? discountAmount : null)
                .idCardCode(request.getIdCardCode())
                .billStatus(Bill.BillStatus.PENDING)
                .paymentMethod(Bill.PaymentMethod.VNPAY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .vnpTxnRef(UUID.randomUUID().toString().replace("-", ""))
                .build();

        for (BillDetail bd : details) {
            bd.setBill(bill);
        }
        bill.setBillDetails(details);
        billRepository.save(bill);

        // Tự động gán phòng và chuyển trạng thái sang OCCUPIED ngay khi tạo đơn PENDING
        // for (BillDetail bd : details) {
        // List<Room> availableRooms = roomRepository.findAvailableRoomsByType(
        // bd.getRoomType().getTypeId(),
        // bill.getCheckInDate(),
        // bill.getCheckOutDate()
        // );
        // if (!availableRooms.isEmpty()) {
        // Room room = availableRooms.get(0);
        // room.setRoomStatus(Room.RoomStatus.OCCUPIED);
        // roomRepository.save(room);
        // roomAssignmentService.assignRoom(bd, room);
        // }
        // }

        // SSE Notify Manager (Gửi sau khi Transaction Commit để đảm bảo DB đã có dữ
        // liệu)
        final Long managerId = hotel.getManager().getUserId();
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            sseService.notifyManager(managerId, "NEW_BOOKING", "Bạn có đơn đặt phòng mới!");
                        }
                    });
        } else {
            sseService.notifyManager(managerId, "NEW_BOOKING", "Bạn có đơn đặt phòng mới!");
        }

        return toResponse(bill);
    }

    private void checkManagerPermission(Bill bill) {

        if (SecurityUtil.hasRole("ADMIN")) {
            return;
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();

        Long managerId = bill.getHotel().getManager().getUserId();

        if (!managerId.equals(currentUserId)) {

            throw new BadRequestException(
                    "Không có quyền thao tác bill này");
        }
    }

    @Override
    public BillResponse cancel(Long billId) {

        Bill bill = getBill(billId);

        Long currentUserId = SecurityUtil.getCurrentUserId();

        boolean isManager = bill.getHotel().getManager().getUserId().equals(currentUserId);

        boolean isUser = bill.getUser().getUserId().equals(currentUserId);

        boolean isAdmin = SecurityUtil.hasRole("ADMIN");

        if (!isManager && !isUser && !isAdmin) {
            throw new BadRequestException("Không có quyền hủy");
        }

        if (bill.getActualCheckInTime() != null) {
            throw new BadRequestException("Đã check-in không thể hủy");
        }

        if (isUser) {
            bill.setCancelReason(Bill.CancelReason.USER_CANCEL);
        } else {
            bill.setCancelReason(Bill.CancelReason.HOTEL_CANCEL);
        }

        bill.setBillStatus(Bill.BillStatus.CANCELED);
        bill.setUpdatedAt(LocalDateTime.now());

        // Nếu đã gán phòng thì phải giải phóng phòng (AVAILABLE)
        var assignments = roomAssignmentService.getByBill(bill);
        for (var a : assignments) {
            Room room = a.getRoom();
            room.setRoomStatus(Room.RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }

        refundVoucher(bill);

        return toResponse(billRepository.save(bill));
    }

    private void refundVoucher(Bill bill) {
        if (bill.getVoucher() != null) {
            Voucher voucher = bill.getVoucher();
            if (voucher.getUsedCount() != null && voucher.getUsedCount() > 0) {
                voucher.setUsedCount(voucher.getUsedCount() - 1);
                voucherRepository.save(voucher);
            }
        }
    }

    @Override
    public BillResponse assignRooms(Long billId, com.example.loudhotel.dto.request.CheckInRequest request) {
        Bill bill = getBill(billId);
        checkManagerPermission(bill);

        if (request != null && request.getRoomIds() != null && !request.getRoomIds().isEmpty()) {
            List<Long> roomIds = request.getRoomIds();
            int totalRequired = bill.getBillDetails().size();

            if (roomIds.size() != totalRequired) {
                throw new BadRequestException(
                        "Số lượng phòng không khớp (" + roomIds.size() + "/" + totalRequired + ")");
            }

            int index = 0;
            roomAssignmentService.deleteByBill(bill);

            for (BillDetail bd : bill.getBillDetails()) {
                Long roomId = roomIds.get(index++);
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Phòng " + roomId + " không tồn tại"));

                if (!room.getRoomType().getTypeId().equals(bd.getRoomType().getTypeId())) {
                    throw new BadRequestException(
                            "Phòng " + room.getRoomNumber() + " không đúng loại " + bd.getRoomType().getTypeName());
                }

                room.setRoomStatus(Room.RoomStatus.OCCUPIED);
                roomRepository.save(room);

                roomAssignmentService.assignRoom(bd, room);
            }
        }
        bill.setUpdatedAt(LocalDateTime.now());
        return toResponse(billRepository.save(bill));
    }

    @Override
    public BillResponse checkIn(Long billId, com.example.loudhotel.dto.request.CheckInRequest request) {

        Bill bill = getBill(billId);
        checkManagerPermission(bill);

        if (bill.getBillStatus() != Bill.BillStatus.PAID) {
            throw new BadRequestException("Chưa thanh toán");
        }

        if (LocalDate.now().isBefore(bill.getCheckInDate())) {
            throw new BadRequestException("Chưa đến ngày nhận phòng");
        }

        // Tự động gán/cập nhật phòng nếu có request roomIds
        if (request != null && request.getRoomIds() != null && !request.getRoomIds().isEmpty()) {
            this.assignRooms(billId, request);
        }

        LocalDateTime now = LocalDateTime.now();
        bill.setActualCheckInTime(now);
        bill.setUpdatedAt(now);

        return toResponse(billRepository.save(bill));
    }

    @Override
    public BillResponse addExtraFee(Long billId, com.example.loudhotel.dto.request.ExtraFeeRequest request) {
        Bill bill = getBill(billId);
        checkManagerPermission(bill);

        BillExtraFee fee = BillExtraFee.builder()
                .bill(bill)
                .amount(request.getAmount())
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .build();

        billExtraFeeRepository.save(fee);

        // Cập nhật total_cost của bill
        if (bill.getExtraFees() == null) {
            bill.setExtraFees(new ArrayList<>());
        }
        bill.getExtraFees().add(fee);
        recalculateAndSaveTotalCost(bill);

        return toResponse(bill);
    }

    @Override
    public BillResponse checkOut(Long billId) {

        Bill bill = getBill(billId);

        checkManagerPermission(bill);

        if (bill.getActualCheckInTime() == null) {
            throw new BadRequestException("Chưa check-in");
        }

        boolean hasUnpaidFees = bill.getExtraFees() != null &&
                bill.getExtraFees().stream().anyMatch(f -> !f.isPaid());

        if (hasUnpaidFees) {
            throw new BadRequestException("Cần thanh toán phụ phí trước khi check-out");
        }

        LocalDateTime now = LocalDateTime.now();

        bill.setActualCheckOutTime(now);
        bill.setUpdatedAt(now);

        // Chuyển trạng thái phòng về AVAILABLE
        var assignments = roomAssignmentService.getByBill(bill);
        for (var a : assignments) {
            Room room = a.getRoom();
            room.setRoomStatus(Room.RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }

        return toResponse(billRepository.save(bill));
    }

    @Scheduled(fixedRate = 60000)
    public void autoCancelExpiredVnpayBills() {

        LocalDateTime timeout = LocalDateTime.now().minusMinutes(10);

        List<Bill> bills = billRepository.findByBillStatusAndCreatedAtBefore(
                Bill.BillStatus.PENDING,
                timeout);

        for (Bill bill : bills) {

            if (bill.getPaymentMethod() == Bill.PaymentMethod.VNPAY) {

                bill.setBillStatus(
                        Bill.BillStatus.CANCELED);

                bill.setCancelReason(
                        Bill.CancelReason.VNPAY_CANCEL);

                bill.setUpdatedAt(
                        LocalDateTime.now());

                refundVoucher(bill);

                // Giải phóng phòng OCCUPIED khi bill bị hủy tự động
                var assignments = roomAssignmentService.getByBill(bill);
                for (var a : assignments) {
                    Room room = a.getRoom();
                    room.setRoomStatus(Room.RoomStatus.AVAILABLE);
                    roomRepository.save(room);
                }
            }
        }

        billRepository.saveAll(bills);
    }

    @Override
    public BillResponse getById(Long id) {
        return toResponse(getBill(id));
    }

    private Bill getBill(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill không tồn tại"));
    }

    @Override
    public List<BillResponse> getByUser(Long userId) {
        return billRepository.findByUser_UserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BillResponse pay(Long billId) {
        Bill bill = getBill(billId);
        checkManagerPermission(bill);
        bill.setBillStatus(Bill.BillStatus.PAID);
        bill.setUpdatedAt(LocalDateTime.now());
        return toResponse(billRepository.save(bill));
    }

    @Override
    public BillResponse payExtraFee(Long billId, Long extraFeeId) {

        Bill bill = getBill(billId);

        checkManagerPermission(bill);

        BillExtraFee fee = billExtraFeeRepository.findById(extraFeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Phụ phí không tồn tại"));

        if (!fee.getBill().getBillId().equals(billId)) {
            throw new BadRequestException("Phụ phí không thuộc đơn hàng này");
        }

        fee.setPaid(true);
        billExtraFeeRepository.saveAndFlush(fee);

        // load lại bill mới từ DB
        Bill freshBill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill không tồn tại"));

        freshBill.setUpdatedAt(LocalDateTime.now());

        return toResponse(billRepository.save(freshBill));
    }

    private String generateBillCode() {

        String prefix = "BLB";

        String datePart = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        while (true) {

            int random = ThreadLocalRandom.current()
                    .nextInt(10000, 100000); // 5 số

            String code = prefix + datePart + random;

            if (!billRepository.existsByBillCode(code)) {
                return code;
            }
        }
    }

    @Override
    public BillResponse update(Long billId, BillRequest request) {

        Bill bill = getBill(billId);

        // CHỈ CHO UPDATE BILL PENDING
        if (bill.getBillStatus() != Bill.BillStatus.PENDING) {
            throw new BadRequestException(
                    "Chỉ được cập nhật đơn chờ thanh toán");
        }

        // rollback voucher cũ nếu có
        refundVoucher(bill);

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel không tồn tại"));

        long nights = ChronoUnit.DAYS.between(
                request.getCheckInDate(),
                request.getCheckOutDate());

        if (nights <= 0) {
            throw new BadRequestException(
                    "Check-out phải sau check-in");
        }

        // XÓA DETAIL CŨ
        bill.getBillDetails().clear();

        List<BillDetail> details = new ArrayList<>();

        double totalCost = 0;

        java.util.Map<Long, Integer> typeToRequestedQty = new java.util.HashMap<>();
        for (BillRequest.RoomSelection r : request.getRooms()) {
            typeToRequestedQty.put(r.getTypeId(), typeToRequestedQty.getOrDefault(r.getTypeId(), 0) + r.getQuantity());
        }

        for (BillRequest.RoomSelection r : request.getRooms()) {

            RoomType roomType = roomTypeRepository.findById(r.getTypeId())
                    .orElseThrow(() -> new RuntimeException("Room type not found"));

            int totalRooms = roomRepository.countTotalRoomsForBooking(
                    roomType.getTypeId());

            int booked = roomRepository.countBookedRooms(
                    roomType.getTypeId(),
                    request.getCheckInDate(),
                    request.getCheckOutDate());

            int available = totalRooms - booked;

            if (available < typeToRequestedQty.get(r.getTypeId())) {
                throw new BadRequestException(
                        "Không đủ phòng trống");
            }

            double price = roomType.getPrice();

            for (int i = 0; i < r.getQuantity(); i++) {

                BillDetail bd = BillDetail.builder()
                        .bill(bill)
                        .roomType(roomType)
                        .priceAtBooking(price)
                        .nights((int) nights)
                        .guestCount(r.getGuestCount())
                        .build();

                details.add(bd);

                totalCost += price * nights;
            }
        }

        double roomTotal = totalCost;

        Double discountAmount = 0.0;

        Voucher appliedVoucher = null;

        /* ===== APPLY VOUCHER MỚI ===== */
        if (request.getVoucherCode() != null
                && !request.getVoucherCode().trim().isEmpty()) {

            Voucher voucher = voucherRepository
                    .findByVoucherCodeAndIsDeletedFalse(
                            request.getVoucherCode())
                    .orElseThrow(() -> new BadRequestException(
                            "Mã giảm giá không hợp lệ"));

            LocalDateTime now = LocalDateTime.now();

            if (voucher.getStatus() != Voucher.VoucherStatus.ACTIVE ||

                    (voucher.getStartDate() != null
                            && now.isBefore(voucher.getStartDate()))
                    ||

                    (voucher.getEndDate() != null
                            && now.isAfter(voucher.getEndDate()))) {

                throw new BadRequestException(
                        "Voucher không khả dụng");
            }

            if (voucher.getQuantity() != null
                    && voucher.getUsedCount() >= voucher.getQuantity()) {

                throw new BadRequestException(
                        "Voucher đã hết lượt");
            }

            if (voucher.getMinBillAmount() != null
                    && roomTotal < voucher.getMinBillAmount()) {

                throw new BadRequestException(
                        "Chưa đủ giá trị tối thiểu");
            }

            if (voucher.getDiscountType() == Voucher.DiscountType.PERCENT) {

                discountAmount = roomTotal
                        * voucher.getDiscountValue()
                        / 100.0;

                if (voucher.getMaxDiscountAmount() != null
                        && discountAmount > voucher.getMaxDiscountAmount()) {

                    discountAmount = voucher.getMaxDiscountAmount();
                }

            } else {

                discountAmount = voucher.getDiscountValue();
            }

            if (discountAmount > roomTotal) {
                discountAmount = roomTotal;
            }

            totalCost = roomTotal - discountAmount;

            voucher.setUsedCount(
                    voucher.getUsedCount() + 1);

            voucherRepository.save(voucher);

            appliedVoucher = voucher;
        }

        // UPDATE INFO
        bill.setHotel(hotel);

        bill.setOrderName(request.getOrderName());
        bill.setOrderEmail(request.getOrderEmail());
        bill.setOrderPhone(request.getOrderPhone());

        bill.setCheckInDate(request.getCheckInDate());
        bill.setCheckOutDate(request.getCheckOutDate());

        bill.setIdCardCode(request.getIdCardCode());

        bill.setVoucher(appliedVoucher);

        bill.setDiscountAmount(
                discountAmount > 0
                        ? discountAmount
                        : null);

        bill.setTotalCost(totalCost);

        bill.setUpdatedAt(LocalDateTime.now());

        bill.setBillDetails(details);

        return toResponse(
                billRepository.save(bill));
    }
}