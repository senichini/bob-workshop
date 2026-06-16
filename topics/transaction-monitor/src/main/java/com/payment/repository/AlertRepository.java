package com.payment.repository;

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
}

// Made with Bob