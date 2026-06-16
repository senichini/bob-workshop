package com.payment.repository;

import com.payment.model.AlertStatus;
import com.payment.model.TransactionAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易警示資料存取介面
 */
@Repository
public interface AlertRepository extends JpaRepository<TransactionAlert, Long> {
    
    /**
     * 根據交易ID查詢警示
     */
    List<TransactionAlert> findByTransactionId(Long transactionId);
    
    /**
     * 根據警示類型查詢警示
     */
    List<TransactionAlert> findByAlertType(String alertType);
    
    /**
     * 根據嚴重程度查詢警示
     */
    List<TransactionAlert> findBySeverity(String severity);
    
    /**
     * 查詢指定時間後的警示
     */
    List<TransactionAlert> findByDetectedAtAfter(LocalDateTime since);
    
    /**
     * 查詢指定時間範圍內的警示
     */
    @Query("SELECT a FROM TransactionAlert a WHERE a.detectedAt BETWEEN :start AND :end")
    List<TransactionAlert> findByDetectedAtBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    /**
     * 根據交易ID和警示類型查詢警示
     */
    List<TransactionAlert> findByTransactionIdAndAlertType(Long transactionId, String alertType);
    
    /**
     * 計算指定交易的警示數量
     */
    long countByTransactionId(Long transactionId);
    
    /**
     * 根據狀態查詢警示
     */
    List<TransactionAlert> findByStatus(AlertStatus status);
    
    /**
     * 根據狀態和嚴重程度查詢警示
     */
    List<TransactionAlert> findByStatusAndSeverity(AlertStatus status, String severity);
    
    /**
     * 計算指定狀態的警示數量
     */
    long countByStatus(AlertStatus status);
    
    /**
     * 計算指定狀態和嚴重程度的警示數量
     */
    long countByStatusAndSeverity(AlertStatus status, String severity);
    
    /**
     * 複合條件查詢警示
     */
    @Query("SELECT a FROM TransactionAlert a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:severity IS NULL OR a.severity = :severity) AND " +
           "(:alertType IS NULL OR a.alertType = :alertType) AND " +
           "(:startDate IS NULL OR a.detectedAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.detectedAt <= :endDate)")
    List<TransactionAlert> searchAlerts(
        @Param("status") AlertStatus status,
        @Param("severity") String severity,
        @Param("alertType") String alertType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 按類型統計警示數量
     */
    @Query("SELECT a.alertType, COUNT(a) FROM TransactionAlert a GROUP BY a.alertType")
    List<Object[]> countByAlertType();
    
    /**
     * 按嚴重程度統計警示數量
     */
    @Query("SELECT a.severity, COUNT(a) FROM TransactionAlert a GROUP BY a.severity")
    List<Object[]> countBySeverity();
    
    /**
     * 按狀態統計警示數量
     */
    @Query("SELECT a.status, COUNT(a) FROM TransactionAlert a GROUP BY a.status")
    List<Object[]> countByStatusGroup();
    
    /**
     * 計算時間範圍內的警示數量
     */
    @Query("SELECT COUNT(a) FROM TransactionAlert a WHERE a.detectedAt BETWEEN :start AND :end")
    long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

// Made with Bob