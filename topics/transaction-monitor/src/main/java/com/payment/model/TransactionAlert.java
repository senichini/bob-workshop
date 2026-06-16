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
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.PENDING;
    
    @Column
    private LocalDateTime resolvedAt;
    
    @Column(length = 100)
    private String resolvedBy;
    
    @Column(length = 1000)
    private String resolutionNote;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (detectedAt == null) {
            detectedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AlertStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// Made with Bob