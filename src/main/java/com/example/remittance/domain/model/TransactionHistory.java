package com.example.remittance.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private Long depositId;

    private Long withdrawalId;

    private long amount;

    private long fee;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public TransactionHistory(TransactionType transactionType, Long depositId, Long withdrawalId, long amount, long fee, LocalDateTime createdAt) {

        this.transactionType = transactionType;
        this.depositId = depositId;
        this.withdrawalId = withdrawalId;
        this.amount = amount;
        this.fee = fee;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

}
