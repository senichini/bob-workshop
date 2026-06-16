package com.payment.service;

import com.payment.dto.AlertSearchRequest;
import com.payment.dto.AlertStatisticsResponse;
import com.payment.dto.AlertStatusUpdateRequest;
import com.payment.model.AlertStatus;
import com.payment.model.TransactionAlert;
import com.payment.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 警示管理服務
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {
    
    private final AlertRepository alertRepository;
    
    /**
     * 更新警示狀態
     * 
     * @param alertId 警示ID
     * @param request 狀態更新請求
     * @return 更新後的警示
     * @throws IllegalArgumentException 當警示不存在時
     */
    @Transactional
    public TransactionAlert updateAlertStatus(Long alertId, AlertStatusUpdateRequest request) {
        log.info("更新警示狀態: alertId={}, status={}", alertId, request.getStatus());
        
        TransactionAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("找不到警示: " + alertId));
        
        alert.setStatus(request.getStatus());
        
        // 如果狀態為已處理或誤報，記錄處理資訊
        if (request.getStatus() == AlertStatus.RESOLVED || 
            request.getStatus() == AlertStatus.FALSE_POSITIVE) {
            alert.setResolvedAt(LocalDateTime.now());
            alert.setResolvedBy(request.getResolvedBy());
            alert.setResolutionNote(request.getResolutionNote());
        }
        
        TransactionAlert savedAlert = alertRepository.save(alert);
        log.info("警示狀態已更新: alertId={}, newStatus={}", alertId, savedAlert.getStatus());
        
        return savedAlert;
    }
    
    /**
     * 標記警示為已處理
     * 
     * @param alertId 警示ID
     * @param resolvedBy 處理人員
     * @param note 處理備註
     * @return 更新後的警示
     * @throws IllegalArgumentException 當警示不存在時
     */
    @Transactional
    public TransactionAlert markAsResolved(Long alertId, String resolvedBy, String note) {
        log.info("標記警示為已處理: alertId={}, resolvedBy={}", alertId, resolvedBy);
        
        TransactionAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("找不到警示: " + alertId));
        
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        alert.setResolutionNote(note);
        
        TransactionAlert savedAlert = alertRepository.save(alert);
        log.info("警示已標記為已處理: alertId={}", alertId);
        
        return savedAlert;
    }
    
    /**
     * 標記警示為誤報
     * 
     * @param alertId 警示ID
     * @param resolvedBy 處理人員
     * @param note 處理備註
     * @return 更新後的警示
     * @throws IllegalArgumentException 當警示不存在時
     */
    @Transactional
    public TransactionAlert markAsFalsePositive(Long alertId, String resolvedBy, String note) {
        log.info("標記警示為誤報: alertId={}, resolvedBy={}", alertId, resolvedBy);
        
        TransactionAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("找不到警示: " + alertId));
        
        alert.setStatus(AlertStatus.FALSE_POSITIVE);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        alert.setResolutionNote(note);
        
        TransactionAlert savedAlert = alertRepository.save(alert);
        log.info("警示已標記為誤報: alertId={}", alertId);
        
        return savedAlert;
    }
    
    /**
     * 根據狀態查詢警示
     * 
     * @param status 警示狀態
     * @return 警示列表
     */
    public List<TransactionAlert> getAlertsByStatus(AlertStatus status) {
        log.info("查詢警示: status={}", status);
        return alertRepository.findByStatus(status);
    }
    
    /**
     * 查詢待處理警示
     * 
     * @return 待處理警示列表
     */
    public List<TransactionAlert> getPendingAlerts() {
        log.info("查詢待處理警示");
        return alertRepository.findByStatus(AlertStatus.PENDING);
    }
    
    /**
     * 根據狀態和嚴重程度查詢警示
     * 
     * @param status 警示狀態
     * @param severity 嚴重程度
     * @return 警示列表
     */
    public List<TransactionAlert> getAlertsByStatusAndSeverity(AlertStatus status, String severity) {
        log.info("查詢警示: status={}, severity={}", status, severity);
        return alertRepository.findByStatusAndSeverity(status, severity);
    }
    
    /**
     * 計算指定狀態的警示數量
     * 
     * @param status 警示狀態
     * @return 警示數量
     */
    public long countByStatus(AlertStatus status) {
        return alertRepository.countByStatus(status);
    }
    
    /**
     * 計算指定狀態和嚴重程度的警示數量
     * 
     * @param status 警示狀態
     * @param severity 嚴重程度
     * @return 警示數量
     */
    public long countByStatusAndSeverity(AlertStatus status, String severity) {
        return alertRepository.countByStatusAndSeverity(status, severity);
    }
    
    /**
     * 取得警示統計總覽
     *
     * @return 統計報表
     */
    public AlertStatisticsResponse getStatistics() {
        log.info("取得警示統計總覽");
        
        long total = alertRepository.count();
        long pending = alertRepository.countByStatus(AlertStatus.PENDING);
        long resolved = alertRepository.countByStatus(AlertStatus.RESOLVED);
        long falsePositive = alertRepository.countByStatus(AlertStatus.FALSE_POSITIVE);
        
        // 按類型統計
        Map<String, Long> byType = new HashMap<>();
        List<Object[]> typeStats = alertRepository.countByAlertType();
        for (Object[] stat : typeStats) {
            byType.put((String) stat[0], (Long) stat[1]);
        }
        
        // 按嚴重程度統計
        Map<String, Long> bySeverity = new HashMap<>();
        List<Object[]> severityStats = alertRepository.countBySeverity();
        for (Object[] stat : severityStats) {
            bySeverity.put((String) stat[0], (Long) stat[1]);
        }
        
        // 按狀態統計
        Map<String, Long> byStatus = new HashMap<>();
        List<Object[]> statusStats = alertRepository.countByStatusGroup();
        for (Object[] stat : statusStats) {
            byStatus.put(stat[0].toString(), (Long) stat[1]);
        }
        
        log.info("統計完成: total={}, pending={}, resolved={}, falsePositive={}",
                 total, pending, resolved, falsePositive);
        
        return AlertStatisticsResponse.builder()
            .total(total)
            .pending(pending)
            .resolved(resolved)
            .falsePositive(falsePositive)
            .byType(byType)
            .bySeverity(bySeverity)
            .byStatus(byStatus)
            .build();
    }
    
    /**
     * 取得時間範圍內的統計
     *
     * @param start 開始時間
     * @param end 結束時間
     * @return 統計報表
     */
    public AlertStatisticsResponse getStatisticsByDateRange(LocalDateTime start, LocalDateTime end) {
        log.info("取得時間範圍統計: start={}, end={}", start, end);
        
        List<TransactionAlert> alerts = alertRepository.findByDetectedAtBetween(start, end);
        
        long total = alerts.size();
        long pending = alerts.stream().filter(a -> a.getStatus() == AlertStatus.PENDING).count();
        long resolved = alerts.stream().filter(a -> a.getStatus() == AlertStatus.RESOLVED).count();
        long falsePositive = alerts.stream().filter(a -> a.getStatus() == AlertStatus.FALSE_POSITIVE).count();
        
        // 按類型統計
        Map<String, Long> byType = new HashMap<>();
        alerts.forEach(alert ->
            byType.merge(alert.getAlertType(), 1L, Long::sum)
        );
        
        // 按嚴重程度統計
        Map<String, Long> bySeverity = new HashMap<>();
        alerts.forEach(alert ->
            bySeverity.merge(alert.getSeverity(), 1L, Long::sum)
        );
        
        // 按狀態統計
        Map<String, Long> byStatus = new HashMap<>();
        alerts.forEach(alert ->
            byStatus.merge(alert.getStatus().toString(), 1L, Long::sum)
        );
        
        log.info("時間範圍統計完成: total={}, pending={}, resolved={}, falsePositive={}",
                 total, pending, resolved, falsePositive);
        
        return AlertStatisticsResponse.builder()
            .total(total)
            .pending(pending)
            .resolved(resolved)
            .falsePositive(falsePositive)
            .byType(byType)
            .bySeverity(bySeverity)
            .byStatus(byStatus)
            .build();
    }
    
    /**
     * 複合條件查詢警示
     *
     * @param request 搜尋條件
     * @return 符合條件的警示列表
     */
    public List<TransactionAlert> searchAlerts(AlertSearchRequest request) {
        log.info("複合條件查詢警示: status={}, severity={}, alertType={}, startDate={}, endDate={}",
                 request.getStatus(), request.getSeverity(), request.getAlertType(),
                 request.getStartDate(), request.getEndDate());
        
        List<TransactionAlert> results = alertRepository.searchAlerts(
            request.getStatus(),
            request.getSeverity(),
            request.getAlertType(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        log.info("查詢完成，找到 {} 筆警示", results.size());
        return results;
    }
    
    /**
     * 取得高風險待處理警示
     *
     * @return 高風險待處理警示列表
     */
    public List<TransactionAlert> getHighRiskPendingAlerts() {
        log.info("查詢高風險待處理警示");
        List<TransactionAlert> alerts = alertRepository.findByStatusAndSeverity(AlertStatus.PENDING, "HIGH");
        log.info("找到 {} 筆高風險待處理警示", alerts.size());
        return alerts;
    }
}

// Made with Bob