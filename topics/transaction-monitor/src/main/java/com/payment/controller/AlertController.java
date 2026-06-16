package com.payment.controller;

import com.payment.model.Transaction;
import com.payment.model.TransactionAlert;
import com.payment.repository.AlertRepository;
import com.payment.service.AlertDetectionService;
import com.payment.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易警示控制器
 *
 * @author IBM Bob Workshop
 * @version 1.0.0
 */
@Tag(name = "警示管理", description = "交易警示查詢 API")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertController {
    
    private final AlertRepository alertRepository;
    private final AlertDetectionService alertDetectionService;
    private final TransactionService transactionService;
    
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
        summary = "查詢未處理警示",
        description = "取得所有警示記錄（目前系統尚未實作警示處理狀態）"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得警示列表",
            content = @Content(schema = @Schema(implementation = TransactionAlert.class))
        )
    })
    @GetMapping("/unresolved")
    public ResponseEntity<List<TransactionAlert>> getUnresolvedAlerts() {
        // 目前返回所有警示，因為尚未實作 resolved 欄位
        return ResponseEntity.ok(alertRepository.findAll());
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
}

// Made with Bob