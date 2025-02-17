package com.example.remittance.domain.repository;

import com.example.remittance.domain.model.TransactionHistory;
import com.example.remittance.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.test.context.TestConstructor;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class TransactionHistoryRepositoryTest {


    private final TransactionHistoryRepository transactionHistoryRepository;

    TransactionHistoryRepositoryTest(TransactionHistoryRepository transactionHistoryRepository) {
        this.transactionHistoryRepository = transactionHistoryRepository;
    }

    private TransactionHistory transaction1;
    private TransactionHistory transaction2;
    private TransactionHistory transaction3;

    @BeforeEach
    public void setUp() {
        transaction1 = TransactionHistory.builder()
                .transactionType(TransactionType.DEPOSIT)
                .depositId(1L)
                .amount(1000L)
                .createdAt(LocalDateTime.of(2023, 10, 1, 10, 0))
                .build();

        transaction2 = TransactionHistory.builder()
                .transactionType(TransactionType.WITHDRAWAL)
                .withdrawalId(1L)
                .amount(500L)
                .createdAt(LocalDateTime.of(2023, 10, 2, 10, 0))
                .build();

        transaction3 = TransactionHistory.builder()
                .transactionType(TransactionType.TRANSFER)
                .withdrawalId(1L)
                .depositId(2L)
                .amount(200L)
                .fee(2L)
                .createdAt(LocalDateTime.of(2023, 10, 2, 15, 0))
                .build();

        transactionHistoryRepository.save(transaction1);
        transactionHistoryRepository.save(transaction2);
        transactionHistoryRepository.save(transaction3);
    }

    @Test
    @DisplayName("일자기준 계좌의 출금액 합계를 조회한다")
    public void testFindSumWithdrawalDay() {
        // Given
        Long withdrawalId = 1L;
        LocalDateTime stdDate = LocalDateTime.of(2023, 10, 2, 0, 0);

        // When
        long sumWithdrawalAmount = transactionHistoryRepository.findSumWidrawalDay(withdrawalId, stdDate);

        // Then
        assertThat(sumWithdrawalAmount).isEqualTo(700L);
    }

    @Test
    @DisplayName("해당 날짜에 출금 내역이 없으면 0원을 반환한다")
    public void testFindSumWithdrawalDay_NoTransactions() {
        // Given
        Long withdrawalId = 1L;
        LocalDateTime stdDate = LocalDateTime.of(2023, 10, 3, 0, 0);

        // When
        long sumWithdrawalAmount = transactionHistoryRepository.findSumWidrawalDay(withdrawalId, stdDate);

        // Then
        assertThat(sumWithdrawalAmount).isEqualTo(0L); // 해당 날짜에 거래 내역이 없어야 함
    }

    @Test
    @DisplayName("계좌의 거래내역을 최신 순으로 조회할 수 있다")
    public void testFindAllByAccountId() {
        // Given
        Long accountId = 1L;

        // When
        List<TransactionHistory> result = transactionHistoryRepository.findAllByAccountId(accountId);

        // Then
        assertThat(result).hasSize(3); // 3개의 거래 내역이 조회되어야 함
        assertThat(result.get(0)).isEqualTo(transaction3); // 가장 최신 거래 내역이 첫 번째로 오는지 확인
        assertThat(result.get(1)).isEqualTo(transaction2);
        assertThat(result.get(2)).isEqualTo(transaction1);
    }
}
