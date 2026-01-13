package com.example.dto.websocket;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardUpdate {
    private String message;
    private List<AdminTableSummary> activeTables;
    

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminTableSummary{
        private Integer tableId;
        private Integer capacity;
        private Integer orderId;
        private String orderStatus;
        private Integer itemCount;
        private LocalDateTime lastUpdate;
    }
}
