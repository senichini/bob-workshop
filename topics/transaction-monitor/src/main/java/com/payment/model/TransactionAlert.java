package com.payment.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * 交易警示實體
 */
@Entity
@Table(name = "transaction_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Transaction transaction;
    
    @Column(nullable = false, length = 50)
    private String alertType;
    
    @Column(nullable = false, length = 20)
    private String severity;
    
    @Column(nullable = false)
    private LocalDateTime detectedAt;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (detectedAt == null) {
            detectedAt = LocalDateTime.now();
        }
    }
}

// Made with Bob