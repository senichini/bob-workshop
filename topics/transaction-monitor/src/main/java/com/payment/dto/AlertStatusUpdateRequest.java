package com.payment.dto;

import com.payment.model.AlertStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 警示狀態更新請求 DTO
 */
@Data
@Schema(description = "警示狀態更新請求")
public class AlertStatusUpdateRequest {
    
    @NotNull(message = "狀態不能為空")
    @Schema(description = "警示狀態", example = "RESOLVED", required = true)
    private AlertStatus status;
    
    @Schema(description = "處理人員", example = "admin")
    private String resolvedBy;
    
    @Schema(description = "處理備註", example = "已確認為正常交易")
    private String resolutionNote;
}

// Made with Bob