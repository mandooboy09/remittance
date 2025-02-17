package com.example.remittance.application.dto;

import com.example.remittance.domain.model.TransactionHistory;
import com.example.remittance.domain.model.TransactionType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record TransactionHistoryDTO(
        Long id,

        TransactionType transactionType,

        Long depositId,

        Long withdrawalId,

        long amount,

        long fee,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
    public static TransactionHistoryDTO from(TransactionHistory transactionHistory) {
        return TransactionHistoryDTO.builder()
                .id(transactionHistory.getId())
                .transactionType(transactionHistory.getTransactionType())
                .depositId(transactionHistory.getDepositId())
                .withdrawalId(transactionHistory.getWithdrawalId())
                .amount(transactionHistory.getAmount())
                .fee(transactionHistory.getFee())
                .createdAt(transactionHistory.getCreatedAt())
                .updatedAt(transactionHistory.getUpdatedAt())
                .build();
    }
}
