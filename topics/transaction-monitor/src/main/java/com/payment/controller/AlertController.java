package com.payment.controller;

import com.payment.dto.AlertSearchRequest;
import com.payment.dto.AlertStatisticsResponse;
import com.payment.dto.AlertStatusUpdateRequest;
import com.payment.model.AlertStatus;
import com.payment.model.Transaction;
import com.payment.model.TransactionAlert;
import com.payment.repository.AlertRepository;
import com.payment.service.AlertDetectionService;
import com.payment.service.AlertService;
import com.payment.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 交易警示控制器
 *
 * @author IBM Bob Workshop
 * @version 1.0.0
 */
@Tag(name = "警示管理", description = "交易警示查詢與管理 API")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertController {
    
    private final AlertRepository alertRepository;
    private final AlertDetectionService alertDetectionService;
    private final TransactionService transactionService;
    private final AlertService alertService;
    
    @Operation(
        summary = "查詢所有警示",
        description = "取得系統中所有的交易警示記錄"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<TransactionAlert>> getAllAlerts() {
        return ResponseEntity.ok(alertRepository.findAll());
    }
    
    @Operation(
        summary = "查詢單筆警示",
        description = "根據警示 ID 查詢特定警示的詳細資訊"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得警示資料",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        ),
        @ApiResponse(responseCode = "404", description = "找不到指定的警示")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionAlert> getAlertById(
            @Parameter(description = "警示 ID", required = true, example = "1")
            @PathVariable Long id) {
        return alertRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(
        summary = "查詢交易的警示",
        description = "取得特定交易的所有警示記錄"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得該交易的警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<TransactionAlert>> getAlertsByTransaction(
            @Parameter(description = "交易 ID", required = true, example = "1")
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(alertRepository.findByTransactionId(transactionId));
    }
    
    @Operation(
        summary = "根據警示類型查詢",
        description = "取得指定類型的所有警示記錄"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @GetMapping("/type/{alertType}")
    public ResponseEntity<List<TransactionAlert>> getAlertsByType(
            @Parameter(description = "警示類型", required = true, example = "HIGH_AMOUNT")
            @PathVariable String alertType) {
        return ResponseEntity.ok(alertRepository.findByAlertType(alertType));
    }
    
    @Operation(
        summary = "根據嚴重程度查詢",
        description = "取得指定嚴重程度的所有警示記錄"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<TransactionAlert>> getAlertsBySeverity(
            @Parameter(description = "嚴重程度", required = true, example = "HIGH")
            @PathVariable String severity) {
        return ResponseEntity.ok(alertRepository.findBySeverity(severity));
    }
    
    @Operation(
        summary = "查詢高風險警示",
        description = "取得所有高風險等級的警示記錄"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得高風險警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @GetMapping("/high-risk")
    public ResponseEntity<List<TransactionAlert>> getHighRiskAlerts() {
        return ResponseEntity.ok(alertRepository.findBySeverity("HIGH"));
    }
    
    @Operation(
        summary = "查詢待處理警示",
        description = "取得所有狀態為 PENDING 的警示記錄"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得待處理警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @GetMapping("/unresolved")
    public ResponseEntity<List<TransactionAlert>> getUnresolvedAlerts() {
        return ResponseEntity.ok(alertService.getPendingAlerts());
    }
    
    @Operation(
        summary = "根據狀態查詢警示",
        description = "取得指定狀態的所有警示記錄"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionAlert>> getAlertsByStatus(
            @Parameter(description = "警示狀態", required = true, example = "PENDING")
            @PathVariable AlertStatus status) {
        return ResponseEntity.ok(alertService.getAlertsByStatus(status));
    }
    
    @Operation(
        summary = "更新警示狀態",
        description = "更新指定警示的狀態及處理資訊"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功更新警示狀態",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        ),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤"),
        @ApiResponse(responseCode = "404", description = "找不到指定的警示")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAlertStatus(
            @Parameter(description = "警示 ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody AlertStatusUpdateRequest request) {
        try {
            TransactionAlert updatedAlert = alertService.updateAlertStatus(id, request);
            return ResponseEntity.ok(updatedAlert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
        summary = "標記警示為已處理",
        description = "將指定警示標記為已處理狀態"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功標記為已處理",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        ),
        @ApiResponse(responseCode = "404", description = "找不到指定的警示")
    })
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> markAsResolved(
            @Parameter(description = "警示 ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String resolvedBy = request.getOrDefault("resolvedBy", "system");
            String note = request.getOrDefault("note", "");
            TransactionAlert updatedAlert = alertService.markAsResolved(id, resolvedBy, note);
            return ResponseEntity.ok(updatedAlert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
        summary = "標記警示為誤報",
        description = "將指定警示標記為誤報狀態"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功標記為誤報",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        ),
        @ApiResponse(responseCode = "404", description = "找不到指定的警示")
    })
    @PutMapping("/{id}/mark-false-positive")
    public ResponseEntity<?> markAsFalsePositive(
            @Parameter(description = "警示 ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String resolvedBy = request.getOrDefault("resolvedBy", "system");
            String note = request.getOrDefault("note", "");
            TransactionAlert updatedAlert = alertService.markAsFalsePositive(id, resolvedBy, note);
            return ResponseEntity.ok(updatedAlert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
        summary = "手動觸發異常偵測",
        description = "對所有現有交易執行異常偵測，產生警示記錄。適合在系統初始化或需要重新分析時使用。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "偵測完成，回傳偵測到的警示數量"
        )
    })
    @PostMapping("/detect")
    public ResponseEntity<java.util.Map<String, Object>> detectAlerts() {
        // 取得所有交易
        java.util.List<Transaction> allTransactions = transactionService.getAllTransactions();
        
        int totalAlerts = 0;
        int highAmountAlerts = 0;
        int frequentAlerts = 0;
        int duplicateAlerts = 0;
        
        // 對每筆交易執行偵測
        for (Transaction transaction : allTransactions) {
            java.util.List<TransactionAlert> alerts = alertDetectionService.detectAlerts(transaction);
            totalAlerts += alerts.size();
            
            // 統計各類型警示數量
            for (TransactionAlert alert : alerts) {
                switch (alert.getAlertType()) {
                    case "HIGH_AMOUNT":
                        highAmountAlerts++;
                        break;
                    case "FREQUENT_TRANSACTIONS":
                        frequentAlerts++;
                        break;
                    case "DUPLICATE_TRANSACTION":
                        duplicateAlerts++;
                        break;
                }
            }
        }
        
        return ResponseEntity.ok(java.util.Map.of(
            "message", "異常偵測完成",
            "totalTransactions", allTransactions.size(),
            "totalAlerts", totalAlerts,
            "highAmountAlerts", highAmountAlerts,
            "frequentAlerts", frequentAlerts,
            "duplicateAlerts", duplicateAlerts
        ));
    }
    
    @Operation(
        summary = "取得警示統計總覽",
        description = "取得所有警示的統計資訊，包含總數、各狀態數量、按類型/嚴重程度/狀態的分組統計"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得統計資料",
            content = @Content(schema = @Schema(implementation = AlertStatisticsResponse.class))
        )
    })
    @GetMapping("/statistics")
    public ResponseEntity<AlertStatisticsResponse> getStatistics() {
        AlertStatisticsResponse statistics = alertService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(
        summary = "取得時間範圍內的統計",
        description = "取得指定時間範圍內的警示統計資訊"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得統計資料",
            content = @Content(schema = @Schema(implementation = AlertStatisticsResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "時間參數格式錯誤")
    })
    @GetMapping("/statistics/range")
    public ResponseEntity<AlertStatisticsResponse> getStatisticsByDateRange(
            @Parameter(description = "開始時間 (ISO 8601 格式)", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "結束時間 (ISO 8601 格式)", required = true, example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        AlertStatisticsResponse statistics = alertService.getStatisticsByDateRange(start, end);
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(
        summary = "複合條件查詢警示",
        description = "使用多個條件組合查詢警示，所有條件都是可選的"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得符合條件的警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @PostMapping("/search")
    public ResponseEntity<List<TransactionAlert>> searchAlerts(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "搜尋條件，所有欄位都是可選的",
                content = @Content(schema = @Schema(implementation = AlertSearchRequest.class))
            )
            @RequestBody AlertSearchRequest request) {
        List<TransactionAlert> results = alertService.searchAlerts(request);
        return ResponseEntity.ok(results);
    }
    
    @Operation(
        summary = "取得高風險待處理警示",
        description = "取得所有嚴重程度為 HIGH 且狀態為 PENDING 的警示"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得高風險待處理警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @GetMapping("/high-risk-pending")
    public ResponseEntity<List<TransactionAlert>> getHighRiskPendingAlerts() {
        List<TransactionAlert> alerts = alertService.getHighRiskPendingAlerts();
        return ResponseEntity.ok(alerts);
    }
}

// Made with Bob