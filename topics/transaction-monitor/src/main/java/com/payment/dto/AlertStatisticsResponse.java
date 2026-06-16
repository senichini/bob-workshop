package com.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertStatisticsResponse {
    private long total;
    private long pending;
    private long resolved;
    private long falsePositive;
    private Map<String, Long> byType;      // 按類型統計
    private Map<String, Long> bySeverity;  // 按嚴重程度統計
    private Map<String, Long> byStatus;    // 按狀態統計
}

// Made with Bob
