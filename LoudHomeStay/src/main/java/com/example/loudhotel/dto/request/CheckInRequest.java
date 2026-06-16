package com.example.loudhotel.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CheckInRequest {
    private List<Long> roomIds;
}
