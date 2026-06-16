package com.payment.controller;

import com.payment.model.TransactionAlert;
import com.payment.repository.AlertRepository;
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
}

// Made with Bob