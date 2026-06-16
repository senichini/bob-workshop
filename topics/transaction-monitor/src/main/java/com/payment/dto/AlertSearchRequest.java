package com.payment.dto;

import com.payment.model.AlertStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AlertSearchRequest {
    private AlertStatus status;
    private String severity;
    private String alertType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

// Made with Bob
