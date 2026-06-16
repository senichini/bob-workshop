package com.payment.service;

import com.payment.model.Transaction;
import com.payment.model.TransactionAlert;
import com.payment.repository.AlertRepository;
import com.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 交易警示偵測服務
 * 實作三種偵測規則：
 * 1. 高額交易：單筆超過 50,000 元
 * 2. 頻繁交易：1 小時內超過 5 筆
 * 3. 重複交易：5 分鐘內相同金額相同商店
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AlertDetectionService {
    
    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    
    // 偵測規則閾值
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000.00");
    private static final int FREQUENT_TRANSACTION_HOURS = 1;
    private static final int FREQUENT_TRANSACTION_COUNT = 5;
    private static final int DUPLICATE_TRANSACTION_MINUTES = 5;
    
    /**
     * 對新交易執行所有偵測規則
     * 
     * @param transaction 要檢查的交易
     * @return 偵測到的警示列表
     */
    public List<TransactionAlert> detectAlerts(Transaction transaction) {
        log.info("開始偵測交易警示: 交易ID={}, 金額={}", 
            transaction.getId(), transaction.getAmount());
        
        List<TransactionAlert> alerts = new ArrayList<>();
        
        // 規則 1: 高額交易偵測
        TransactionAlert highAmountAlert = detectHighAmountTransaction(transaction);
        if (highAmountAlert != null) {
            alerts.add(highAmountAlert);
        }
        
        // 規則 2: 頻繁交易偵測
        TransactionAlert frequentAlert = detectFrequentTransactions(transaction);
        if (frequentAlert != null) {
            alerts.add(frequentAlert);
        }
        
        // 規則 3: 重複交易偵測
        TransactionAlert duplicateAlert = detectDuplicateTransaction(transaction);
        if (duplicateAlert != null) {
            alerts.add(duplicateAlert);
        }
        
        // 儲存所有偵測到的警示
        if (!alerts.isEmpty()) {
            alerts = alertRepository.saveAll(alerts);
            log.warn("偵測到 {} 個警示: 交易ID={}", alerts.size(), transaction.getId());
        } else {
            log.info("未偵測到警示: 交易ID={}", transaction.getId());
        }
        
        return alerts;
    }
    
    /**
     * 規則 1: 高額交易偵測
     * 單筆交易金額超過 50,000 元
     * 
     * @param transaction 要檢查的交易
     * @return 如果偵測到異常則回傳警示，否則回傳 null
     */
    private TransactionAlert detectHighAmountTransaction(Transaction transaction) {
        if (transaction.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            log.warn("偵測到高額交易: 交易ID={}, 金額={}", 
                transaction.getId(), transaction.getAmount());
            
            TransactionAlert alert = new TransactionAlert();
            alert.setTransaction(transaction);
            alert.setAlertType("HIGH_AMOUNT");
            alert.setSeverity("HIGH");
            alert.setDetectedAt(LocalDateTime.now());
            alert.setDescription(String.format(
                "高額交易警示：交易金額 %s 元超過閾值 %s 元",
                transaction.getAmount(),
                HIGH_AMOUNT_THRESHOLD
            ));
            
            return alert;
        }
        return null;
    }
    
    /**
     * 規則 2: 頻繁交易偵測
     * 同一張卡片在 1 小時內超過 5 筆交易
     * 
     * @param transaction 要檢查的交易
     * @return 如果偵測到異常則回傳警示，否則回傳 null
     */
    private TransactionAlert detectFrequentTransactions(Transaction transaction) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(FREQUENT_TRANSACTION_HOURS);
        
        // 查詢該卡片在過去 1 小時內的交易數量（包含當前交易）
        long recentTransactionCount = transactionRepository
            .countByCardIdAndTransactionTimeAfter(
                transaction.getCard().getId(), 
                oneHourAgo
            );
        
        if (recentTransactionCount > FREQUENT_TRANSACTION_COUNT) {
            log.warn("偵測到頻繁交易: 卡片ID={}, 1小時內交易數={}", 
                transaction.getCard().getId(), recentTransactionCount);
            
            TransactionAlert alert = new TransactionAlert();
            alert.setTransaction(transaction);
            alert.setAlertType("FREQUENT_TRANSACTIONS");
            alert.setSeverity("MEDIUM");
            alert.setDetectedAt(LocalDateTime.now());
            alert.setDescription(String.format(
                "頻繁交易警示：卡片 %s 在 %d 小時內已有 %d 筆交易（閾值：%d 筆）",
                transaction.getCard().getMaskedCardNumber(),
                FREQUENT_TRANSACTION_HOURS,
                recentTransactionCount,
                FREQUENT_TRANSACTION_COUNT
            ));
            
            return alert;
        }
        return null;
    }
    
    /**
     * 規則 3: 重複交易偵測
     * 5 分鐘內相同卡片、相同商店、相同金額的交易
     * 
     * @param transaction 要檢查的交易
     * @return 如果偵測到異常則回傳警示，否則回傳 null
     */
    private TransactionAlert detectDuplicateTransaction(Transaction transaction) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now()
            .minusMinutes(DUPLICATE_TRANSACTION_MINUTES);
        LocalDateTime now = LocalDateTime.now();
        
        // 查詢可能重複的交易（排除當前交易本身）
        List<Transaction> potentialDuplicates = transactionRepository
            .findPotentialDuplicates(
                transaction.getCard().getId(),
                transaction.getMerchant().getId(),
                transaction.getAmount(),
                fiveMinutesAgo,
                now
            );
        
        // 排除當前交易本身
        potentialDuplicates.removeIf(t -> t.getId().equals(transaction.getId()));
        
        if (!potentialDuplicates.isEmpty()) {
            log.warn("偵測到重複交易: 交易ID={}, 發現 {} 筆相似交易", 
                transaction.getId(), potentialDuplicates.size());
            
            TransactionAlert alert = new TransactionAlert();
            alert.setTransaction(transaction);
            alert.setAlertType("DUPLICATE_TRANSACTION");
            alert.setSeverity("HIGH");
            alert.setDetectedAt(LocalDateTime.now());
            alert.setDescription(String.format(
                "重複交易警示：在 %d 分鐘內偵測到 %d 筆相同金額（%s 元）、相同商店（%s）的交易",
                DUPLICATE_TRANSACTION_MINUTES,
                potentialDuplicates.size() + 1,
                transaction.getAmount(),
                transaction.getMerchant().getMerchantName()
            ));
            
            return alert;
        }
        return null;
    }
    
    /**
     * 查詢所有警示
     * 
     * @return 所有警示列表
     */
    @Transactional(readOnly = true)
    public List<TransactionAlert> getAllAlerts() {
        return alertRepository.findAll();
    }
    
    /**
     * 根據交易ID查詢警示
     * 
     * @param transactionId 交易ID
     * @return 該交易的警示列表
     */
    @Transactional(readOnly = true)
    public List<TransactionAlert> getAlertsByTransactionId(Long transactionId) {
        return alertRepository.findByTransactionId(transactionId);
    }
    
    /**
     * 根據警示類型查詢警示
     * 
     * @param alertType 警示類型
     * @return 該類型的警示列表
     */
    @Transactional(readOnly = true)
    public List<TransactionAlert> getAlertsByType(String alertType) {
        return alertRepository.findByAlertType(alertType);
    }
    
    /**
     * 根據嚴重程度查詢警示
     * 
     * @param severity 嚴重程度
     * @return 該嚴重程度的警示列表
     */
    @Transactional(readOnly = true)
    public List<TransactionAlert> getAlertsBySeverity(String severity) {
        return alertRepository.findBySeverity(severity);
    }
    
    /**
     * 查詢最近的警示
     * 
     * @param hours 小時數
     * @return 最近的警示列表
     */
    @Transactional(readOnly = true)
    public List<TransactionAlert> getRecentAlerts(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return alertRepository.findByDetectedAtAfter(since);
    }
}

// Made with Bob