package com.example.loudhotel.service.impl;

import com.example.loudhotel.dto.request.ChatbotRequest;
import com.example.loudhotel.dto.response.*;
import com.example.loudhotel.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final HotelService hotelService;
    private final RoomTypeService roomTypeService;
    private final RoomService roomService;
    private final VoucherService voucherService;
    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model:llama-3.1-8b-instant}")
    private String model;

    @Override
    public ChatbotResponse chat(ChatbotRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        List<ObjectNode> messages = new ArrayList<>();

        // 1. Thêm System Prompt định hướng chatbot với đầy đủ nghiệp vụ tĩnh (FAQ)
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Bạn là trợ lý ảo hỗ trợ tư vấn đặt phòng của hệ thống khách sạn LoudHotel (LoudBooking.com).\n" +
                "Nhiệm vụ của bạn là hỗ trợ khách hàng tìm kiếm khách sạn, phòng trống, tư vấn loại phòng, đánh giá và khuyến mãi.\n\n" +
                "1. HƯỚNG DẪN ĐẶT PHÒNG & THANH TOÁN:\n" +
                "   - Quy trình đặt phòng: Tìm khách sạn/phòng -> Bấm nút 'Đặt phòng' -> Nhập thông tin lưu trú, áp dụng Voucher -> Chọn thanh toán trực tuyến qua VNPay hoặc Giữ chỗ (Hold) -> Thanh toán thành công sẽ nhận được email xác nhận & hóa đơn.\n" +
                "2. CHÍNH SÁCH NHẬN/TRẢ PHÒNG & HỦY PHÒNG:\n" +
                "   - Giờ check-in: Từ 14:00. Giờ check-out: Trước 12:00 trưa.\n" +
                "   - Chính sách hủy phòng: Miễn phí hủy phòng trước 24h so với ngày nhận phòng. Nếu hủy trong vòng 24h trước khi check-in sẽ không được hoàn tiền.\n\n" +
                "QUY ĐỊNH GỌI TOOL & HIỂN THỊ:\n" +
                "   - Quy trình tìm phòng trống theo theo ngày nhận/trả và số lượng phòng cần đặt (Ví dụ: tôi cần tìm 10 phòng ở Hà Nội từ ngày A đến ngày B): Bạn KHÔNG ĐƯỢC hỏi người dùng mã/ID khách sạn.\n" +
                "KHI người dùng hỏi tìm phòng theo địa điểm" +
                "(ví dụ: Hà Nội, Đà Nẵng, Hồ Chí Minh...)" +

                "TUYỆT ĐỐI KHÔNG ĐƯỢC gọi get_room_types trực tiếp." +

                "LUÔN LUÔN thực hiện:" +

                "Bước 1:" +
                "Gọi get_available_hotels(keyword, checkIn, checkOut, roomCount)" +

                "Bước 2:" +
                "Lấy hotelId từ kết quả bước 1." +

                "Bước 3:" +
                "Gọi get_room_types(hotelId)." +

                "Nếu chưa có hotelId thì KHÔNG ĐƯỢC gọi get_room_types." +
                "   - Quy định về Năm: Khi khách hàng cung cấp ngày tháng không có năm (ví dụ: '25/5'), hãy tự động lấy năm hiện tại (2026) để định dạng thành YYYY-MM-DD (ví dụ: '2026-05-25') trước khi gọi tool.\n" +
                "   - Bạn chỉ được sử dụng các công cụ (tools) được khai báo. TUYỆT ĐỐI KHÔNG ĐƯỢC gọi các công cụ ngoài danh sách như 'brave_search', 'web_search'.\n" +
                "   - Với câu hỏi hướng dẫn đặt phòng, chính sách nhận/trả phòng, hủy phòng và thanh toán, hãy tự trả lời trực tiếp dựa trên tri thức nghiệp vụ trên mà không được gọi bất kỳ tool nào.\n" +
                "   - Không hiển thị tên tool, tham số tool hoặc dữ liệu JSON cho người dùng. Chỉ trả lời bằng ngôn ngữ tự nhiên.\n\n" +
                "   - Không cần hiển thị các bước thức hiện ra màn hình" +
                "Hãy sử dụng các công cụ (tools) được cung cấp để tra cứu thông tin thực tế từ Database. KHÔNG ĐƯỢC tự bịa ra thông tin. Trả lời lịch sự bằng Tiếng Việt. Định dạng rõ ràng, ngắn gọn, dùng gạch đầu dòng.");
        messages.add(systemMessage);

        // 2. Thêm lịch sử hội thoại
        if (request.getHistory() != null) {
            for (ChatbotRequest.Message msg : request.getHistory()) {
                ObjectNode historyMessage = objectMapper.createObjectNode();
                historyMessage.put("role", msg.getRole());
                historyMessage.put("content", msg.getContent());
                messages.add(historyMessage);
            }
        }

        // 3. Thêm tin nhắn hiện tại của người dùng
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", request.getMessage());
        messages.add(userMessage);

        int maxLoops = 5;
        String finalReply = "Xin lỗi anh, hệ thống đang bận xử lý hoặc gặp sự cố kết nối AI. Anh vui lòng thử lại sau nhé.";

        try {
            for (int loop = 0; loop < maxLoops; loop++) {
                log.info("Gửi request lên Groq AI (Vòng {})...", loop + 1);

                ObjectNode payload = objectMapper.createObjectNode();
                payload.put("model", model);
                payload.put("temperature", 0);

                ArrayNode messagesNode = objectMapper.createArrayNode();
                messages.forEach(messagesNode::add);
                payload.set("messages", messagesNode);

                payload.set("tools", getToolsDefinition());
                payload.put("tool_choice", "auto");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<String> httpEntity = new HttpEntity<>(payload.toString(), headers);

                log.info("=== PAYLOAD ===");
                log.info(payload.toPrettyString());
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, httpEntity, String.class);
                JsonNode responseJson = objectMapper.readTree(responseEntity.getBody());
                log.info("=== RESPONSE ===");
                log.info(responseEntity.getBody());

                if (responseJson == null || !responseJson.has("choices")) {
                    log.error("Groq API Response không hợp lệ: {}", responseEntity.getBody());
                    break;
                }

                JsonNode choice = responseJson.get("choices").get(0);
                JsonNode messageNode = choice.get("message");
                String content = messageNode.has("content") && !messageNode.get("content").isNull()
                        ? messageNode.get("content").asText() : "";

                ObjectNode aiResponseMsg = objectMapper.createObjectNode();
                aiResponseMsg.put("role", "assistant");
                if (!content.isEmpty()) {
                    aiResponseMsg.put("content", content);
                } else {
                    aiResponseMsg.putNull("content");
                }

                if (messageNode.has("tool_calls") && !messageNode.get("tool_calls").isNull() && messageNode.get("tool_calls").size() > 0) {
                    JsonNode toolCalls = messageNode.get("tool_calls");
                    aiResponseMsg.set("tool_calls", toolCalls);
                    messages.add(aiResponseMsg);

                    log.info("AI yêu cầu gọi {} tool(s)", toolCalls.size());

                    for (JsonNode toolCall : toolCalls) {
                        String toolCallId = toolCall.get("id").asText();
                        String functionName = toolCall.get("function").get("name").asText();
                        String arguments = toolCall.get("function").get("arguments").asText();

                        log.info("Thực thi Tool: {}, Arguments: {}", functionName, arguments);
                        String toolResult = executeTool(functionName, arguments);

                        ObjectNode toolResponseMsg = objectMapper.createObjectNode();
                        toolResponseMsg.put("role", "tool");
                        toolResponseMsg.put("tool_call_id", toolCallId);
                        toolResponseMsg.put("name", functionName);
                        toolResponseMsg.put("content", toolResult);
                        messages.add(toolResponseMsg);
                    }
                    continue;
                } else {
                    finalReply = content;
                    messages.add(aiResponseMsg);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Lỗi chatbot: ", e);
            finalReply = "Đã xảy ra lỗi hệ thống khi kết nối tới chatbot tư vấn. Chi tiết lỗi: " + e.getMessage();
        }

        return ChatbotResponse.builder()
                .reply(finalReply)
                .build();
    }

    /**
     * Định nghĩa các Tools tối giản
     */
    private ArrayNode getToolsDefinition() {
        ArrayNode tools = objectMapper.createArrayNode();

        // 1. search_hotels
        ObjectNode searchHotels = objectMapper.createObjectNode();
        searchHotels.put("type", "function");
        ObjectNode shFunc = objectMapper.createObjectNode();
        shFunc.put("name", "search_hotels");
        shFunc.put("description", "Tìm khách sạn theo tên hoặc địa chỉ/thành phố");
        ObjectNode shParams = objectMapper.createObjectNode();
        shParams.put("type", "object");
        ObjectNode shProperties = objectMapper.createObjectNode();
        ObjectNode kwProp = objectMapper.createObjectNode();
        kwProp.put("type", "string");
        kwProp.put("description", "Từ khóa tìm kiếm");
        shProperties.set("keyword", kwProp);
        shParams.set("properties", shProperties);
        ArrayNode shReq = objectMapper.createArrayNode();
        shReq.add("keyword");
        shParams.set("required", shReq);
        shFunc.set("parameters", shParams);
        searchHotels.set("function", shFunc);
        tools.add(searchHotels);

        // 2. get_hotel_details
        ObjectNode getHotel = objectMapper.createObjectNode();
        getHotel.put("type", "function");
        ObjectNode ghFunc = objectMapper.createObjectNode();
        ghFunc.put("name", "get_hotel_details");
        ghFunc.put("description", "Xem thông tin chi tiết một khách sạn");
        ObjectNode ghParams = objectMapper.createObjectNode();
        ghParams.put("type", "object");
        ObjectNode ghProperties = objectMapper.createObjectNode();
        ObjectNode idProp = objectMapper.createObjectNode();
        idProp.put("type", "integer");
        idProp.put("description", "ID khách sạn");
        ghProperties.set("hotelId", idProp);
        ghParams.set("properties", ghProperties);
        ArrayNode ghReq = objectMapper.createArrayNode();
        ghReq.add("hotelId");
        ghParams.set("required", ghReq);
        ghFunc.set("parameters", ghParams);
        getHotel.set("function", ghFunc);
        tools.add(getHotel);

        // 3. get_room_types
        ObjectNode getRoomTypes = objectMapper.createObjectNode();
        getRoomTypes.put("type", "function");
        ObjectNode grtFunc = objectMapper.createObjectNode();
        grtFunc.put("name", "get_room_types");
        grtFunc.put("description",
                "Xem danh sách loại phòng của một khách sạn. " +
                        "QUAN TRỌNG: Chỉ gọi tool này SAU KHI đã gọi get_available_hotels " +
                        "và lấy được hotelId từ kết quả trả về. " +
                        "TUYỆT ĐỐI KHÔNG được tự tạo ra hotelId.");
        ObjectNode grtParams = objectMapper.createObjectNode();
        grtParams.put("type", "object");
        ObjectNode grtProperties = objectMapper.createObjectNode();
        ObjectNode grtIdProp = objectMapper.createObjectNode();
        grtIdProp.put("type", "integer");
        grtIdProp.put("description", "ID khách sạn");
        grtProperties.set("hotelId", grtIdProp);
        grtParams.set("properties", grtProperties);
        ArrayNode grtReq = objectMapper.createArrayNode();
        grtReq.add("hotelId");
        grtParams.set("required", grtReq);
        grtFunc.set("parameters", grtParams);
        getRoomTypes.set("function", grtFunc);
        tools.add(getRoomTypes);

        // 4. get_available_hotels
        ObjectNode getAvailRooms = objectMapper.createObjectNode();
        getAvailRooms.put("type", "function");

        ObjectNode garFunc = objectMapper.createObjectNode();
        garFunc.put("name", "get_available_hotels");
        garFunc.put("description", "Tìm khách sạn còn đủ số lượng phòng trống theo địa điểm, ngày nhận phòng, ngày trả phòng và số lượng phòng cần đặt. Trả về danh sách khách sạn cùng hotelId.");

        ObjectNode garParams = objectMapper.createObjectNode();
        garParams.put("type", "object");

        ObjectNode garProperties = objectMapper.createObjectNode();

        ObjectNode garIdProp = objectMapper.createObjectNode();
        garIdProp.put("type", "string");
        garIdProp.put("description", "tên địa điểm hoặc tên khách sạn");
        garProperties.set("keyword", garIdProp);

        ObjectNode ciProp = objectMapper.createObjectNode();
        ciProp.put("type", "string");
        ciProp.put("description", "Ngày nhận phòng (YYYY-MM-DD)");
        garProperties.set("checkIn", ciProp);

        ObjectNode coProp = objectMapper.createObjectNode();
        coProp.put("type", "string");
        coProp.put("description", "Ngày trả phòng (YYYY-MM-DD)");
        garProperties.set("checkOut", coProp);

        ObjectNode roomCountProp = objectMapper.createObjectNode();
        roomCountProp.put("type", "integer");
        roomCountProp.put("description", "Số lượng phòng cần đặt");
        garProperties.set("roomCount", roomCountProp);

        garParams.set("properties", garProperties);

        ArrayNode garReq = objectMapper.createArrayNode();
        garReq.add("keyword");
        garReq.add("checkIn");
        garReq.add("checkOut");
        garReq.add("roomCount");

        garParams.set("required", garReq);

        garFunc.set("parameters", garParams);
        getAvailRooms.set("function", garFunc);
        tools.add(getAvailRooms);

        // 5. get_vouchers
        ObjectNode getVouchers = objectMapper.createObjectNode();
        getVouchers.put("type", "function");
        ObjectNode gvFunc = objectMapper.createObjectNode();
        gvFunc.put("name", "get_vouchers");
        gvFunc.put("description", "Xem các mã giảm giá của một khách sạn cụ thể");
        ObjectNode gvParams = objectMapper.createObjectNode();
        gvParams.put("type", "object");
        ObjectNode gvProperties = objectMapper.createObjectNode();
        ObjectNode gvIdProp = objectMapper.createObjectNode();
        gvIdProp.put("type", "integer");
        gvIdProp.put("description", "ID khách sạn");
        gvProperties.set("hotelId", gvIdProp);
        gvParams.set("properties", gvProperties);
        ArrayNode gvReq = objectMapper.createArrayNode();
        gvReq.add("hotelId");
        gvParams.set("required", gvReq);
        gvFunc.set("parameters", gvParams);
        getVouchers.set("function", gvFunc);
        tools.add(getVouchers);

        // 6. get_reviews
        ObjectNode getReviews = objectMapper.createObjectNode();
        getReviews.put("type", "function");
        ObjectNode grFunc = objectMapper.createObjectNode();
        grFunc.put("name", "get_reviews");
        grFunc.put("description", "Xem đánh giá của khách hàng về khách sạn");
        ObjectNode grParams = objectMapper.createObjectNode();
        grParams.put("type", "object");
        ObjectNode grProperties = objectMapper.createObjectNode();
        ObjectNode grIdProp = objectMapper.createObjectNode();
        grIdProp.put("type", "string");
        grIdProp.put("description", "ID khách sạn");
        grProperties.set("hotelId", grIdProp);
        grParams.set("properties", grProperties);
        ArrayNode grReq = objectMapper.createArrayNode();
        grReq.add("hotelId");
        grParams.set("required", grReq);
        grFunc.set("parameters", grParams);
        getReviews.set("function", grFunc);
        tools.add(getReviews);

        // 7. get_all_vouchers (Tool mới)
        ObjectNode getAllVouchers = objectMapper.createObjectNode();
        getAllVouchers.put("type", "function");
        ObjectNode gavFunc = objectMapper.createObjectNode();
        gavFunc.put("name", "get_all_vouchers");
        gavFunc.put("description", "Xem tất cả mã giảm giá/khuyến mãi hiện có trên toàn hệ thống");
        ObjectNode gavParams = objectMapper.createObjectNode();
        gavParams.put("type", "object");
        gavParams.set("properties", objectMapper.createObjectNode());
        gavFunc.set("parameters", gavParams);
        getAllVouchers.set("function", gavFunc);
        tools.add(getAllVouchers);

        return tools;
    }

    /**
     * Thực thi Tool và trích lọc dữ liệu tối giản
     */
    private String executeTool(String functionName, String argumentsJson) {
        try {
            JsonNode argsNode = objectMapper.readTree(argumentsJson);
            switch (functionName) {
                case "search_hotels": {
                    String keyword = argsNode.get("keyword").asText();
                    List<HotelResponse> hotels = hotelService.searchAll(keyword);
                    return filterHotels(hotels);
                }
                case "get_hotel_details": {
                    Long hotelId = Long.parseLong(argsNode.get("hotelId").asText());
                    HotelResponse hotel = hotelService.getHotelById(hotelId);
                    return filterHotelDetail(hotel);
                }
                case "get_room_types": {
                    Long hotelId = argsNode.get("hotelId").asLong();
                    List<RoomTypeResponse> roomTypes = roomTypeService.getByHotel(hotelId);
                    return filterRoomTypes(roomTypes);
                }
                case "get_available_hotels": {
                    String keyword = argsNode.get("keyword").asText();
                    String checkInStr = argsNode.get("checkIn").asText();
                    String checkOutStr = argsNode.get("checkOut").asText();
                    Integer roomCount = Integer.parseInt(argsNode.get("roomCount").asText());

                    LocalDate checkIn = LocalDate.parse(checkInStr);
                    LocalDate checkOut = LocalDate.parse(checkOutStr);

                    List<HotelSearchResponse> hotels = hotelService.searchAvailableHotels(keyword, checkIn, checkOut, roomCount);
                    return filterRooms(hotels);
                }
                case "get_vouchers": {
                    Long hotelId = Long.parseLong(argsNode.get("hotelId").asText());
                    List<VoucherResponse> vouchers = voucherService.getPublicVouchers(hotelId);
                    return filterVouchers(vouchers);
                }
                case "hotels_vouchers": {
                    List<VoucherResponse> vouchers = voucherService.getAllVouchers();
                    return filterVouchers(vouchers);
                }
                case "get_reviews": {
                    Long hotelId = Long.parseLong(argsNode.get("hotelId").asText());
                    List<ReviewResponse> reviews = reviewService.getReviewsByHotel(hotelId);
                    return filterReviews(reviews);
                }
                default:
                    return "{\"error\": \"Không tìm thấy tool phù hợp: " + functionName + "\"}";
            }
        } catch (Exception e) {
            log.error("Lỗi khi thực thi tool {}: ", functionName, e);
            return "{\"error\": \"Lỗi khi thực thi tool " + functionName + ": " + e.getMessage() + "\"}";
        }
    }

    // ===== HÀM LỌC DỮ LIỆU TỐI GIẢN TẬN DỤNG TOKEN CỰC TỐT =====

    private String filterHotels(List<HotelResponse> hotels) {
        ArrayNode array = objectMapper.createArrayNode();
        for (HotelResponse h : hotels) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("hotelId", h.getHotelId());
            node.put("hotelName", h.getHotelName());
            node.put("address", h.getAddress());
            node.put("rating", h.getAverageRating());

            // Tìm minPrice từ RoomTypes của khách sạn đó để AI có thể tư vấn khoảng giá
            double minPrice = Double.MAX_VALUE;
            try {
                List<RoomTypeResponse> roomTypes = roomTypeService.getByHotel(h.getHotelId());
                if (roomTypes != null) {
                    for (RoomTypeResponse rt : roomTypes) {
                        if (rt.getPrice() != null && rt.getPrice() < minPrice) {
                            minPrice = rt.getPrice();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Lỗi khi lấy khoảng giá cho khách sạn ID {}: ", h.getHotelId(), e);
            }
            node.put("minPrice", minPrice == Double.MAX_VALUE ? 0.0 : minPrice);

            // Gửi kèm danh sách tiện ích của khách sạn để AI có thể tìm theo tiện ích
            if (h.getUtilities() != null) {
                ArrayNode utils = objectMapper.createArrayNode();
                h.getUtilities().forEach(utils::add);
                node.set("utilities", utils);
            }
            array.add(node);
        }
        return array.toString();
    }

    private String filterHotelDetail(HotelResponse h) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("hotelId", h.getHotelId());
        node.put("hotelName", h.getHotelName());
        node.put("address", h.getAddress());
        node.put("introduction", h.getIntroduction());
        node.put("rating", h.getAverageRating());
        if (h.getUtilities() != null) {
            ArrayNode utils = objectMapper.createArrayNode();
            h.getUtilities().forEach(utils::add);
            node.set("utilities", utils);
        }
        return node.toString();
    }

    private String filterRoomTypes(List<RoomTypeResponse> roomTypes) {
        ArrayNode array = objectMapper.createArrayNode();
        for (RoomTypeResponse r : roomTypes) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("typeId", r.getTypeId());
            node.put("typeName", r.getTypeName());
            node.put("capacity", r.getCapacity());
            node.put("price", r.getPrice());
            node.put("description", r.getDescription());
            node.put("bedCount", r.getBedCount());
            node.put("bedType", r.getBedType());
            node.put("area", r.getArea());

            // Gửi kèm tiện ích của phòng để AI tư vấn phòng theo tiện ích/dịch vụ
            if (r.getUtilities() != null) {
                ArrayNode utils = objectMapper.createArrayNode();
                for (UtilitiesResponse u : r.getUtilities()) {
                    utils.add(u.getName());
                }
                node.set("utilities", utils);
            }
            array.add(node);
        }
        return array.toString();
    }

    private String filterRooms(List<HotelSearchResponse> rooms) {
        ArrayNode array = objectMapper.createArrayNode();
        for (HotelSearchResponse r : rooms) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("hotelId", r.getHotelId());
            node.put("address", r.getAddress());
            node.put("hotelName", r.getHotelName());
            node.put("averageRating", r.getAverageRating());
            node.put("mainImage", r.getMainImage());
            node.put("minPrice", r.getMinPrice());
            node.put("availableRooms", r.getAvailableRooms());
            array.add(node);
        }
        return array.toString();
    }

    private String filterVouchers(List<VoucherResponse> vouchers) {
        ArrayNode array = objectMapper.createArrayNode();
        for (VoucherResponse v : vouchers) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("voucherId", v.getVoucherId());
            node.put("voucherCode", v.getVoucherCode());
            node.put("title", v.getTitle());
            node.put("discountType", v.getDiscountType() != null ? v.getDiscountType().toString() : "");
            node.put("discountValue", v.getDiscountValue());
            node.put("minBillAmount", v.getMinBillAmount());
            node.put("endDate", v.getEndDate() != null ? v.getEndDate().toString() : "");
            array.add(node);
        }
        return array.toString();
    }

    private String filterReviews(List<ReviewResponse> reviews) {
        ArrayNode array = objectMapper.createArrayNode();
        int count = 0;
        for (ReviewResponse r : reviews) {
            if (count >= 3) break; // Chỉ lấy tối đa 3 đánh giá để tiết kiệm token
            ObjectNode node = objectMapper.createObjectNode();
            node.put("username", r.getUsername());
            node.put("rate", r.getRate());
            node.put("comment", r.getComment());
            array.add(node);
            count++;
        }
        return array.toString();
    }
}
