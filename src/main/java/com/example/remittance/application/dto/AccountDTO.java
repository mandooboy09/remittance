package com.example.remittance.application.dto;

import com.example.remittance.domain.model.Account;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record AccountDTO(
        Long id,

        long balanceAmount,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
    public static AccountDTO from(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .balanceAmount(account.getBalanceAmount())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
