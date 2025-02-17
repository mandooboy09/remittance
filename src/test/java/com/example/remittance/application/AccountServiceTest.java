package com.example.remittance.application;

import com.example.remittance.application.dto.TransactionHistoryDTO;
import com.example.remittance.application.service.AccountService;
import com.example.remittance.domain.model.Account;
import com.example.remittance.domain.model.TransactionHistory;
import com.example.remittance.domain.repository.AccountRepository;
import com.example.remittance.domain.repository.TransactionHistoryRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Spy
    private Account account;

    @Spy
    private Account depositedAccount;

    @Test
    @DisplayName("계좌를 생성할 수 있다")
    void success_create_account() {

        accountService.createAccount();

        then(accountRepository).should(times(1)).save(any());
    }

    @Test
    @DisplayName("계좌의 잔액이 0원이면 계좌를 삭제 할 수 있다")
    void success_delete_account_when_balance_zero() {
        given(accountRepository.findById(any())).willReturn(Optional.of(account));
        given(account.getBalanceAmount()).willReturn(0L);

        accountService.deleteAccount(any());

        then(accountRepository).should(times(1)).findById(any());
        then(accountRepository).should(times(1)).delete(any());
    }

    @Test
    @DisplayName("계좌의 잔액이 0원이 아니면 계좌를 삭제 할 수 없다")
    void fail_delete_account_when_balance_not_zero() {
        given(accountRepository.findById(any())).willReturn(Optional.of(account));
        given(account.getBalanceAmount()).willReturn(100L);

        assertThatThrownBy(() -> accountService.deleteAccount(any()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("impossible delete account because exists balance-amount");

        then(accountRepository).should(times(1)).findById(any());
        then(accountRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("계좌가 존재하면 돈을 입금할 수 있다")
    void success_deposit() {
        account.deposit(500L);
        given(accountRepository.findById(any())).willReturn(Optional.of(account));

        accountService.deposit(any(), 1000L);

        assertThat(account.getBalanceAmount()).isEqualTo(1500L);
        then(accountRepository).should(times(1)).findById(any());
        then(transactionHistoryRepository).should(times(1)).save(any());

    }

    @Test
    @DisplayName("계좌가 존재하지 않으면 돈을 입금할 수 없다")
    void fail_deposit() {

        assertThatThrownBy(() -> accountService.deposit(any(), 1000L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("not found account");

        then(accountRepository).should(times(1)).findById(any());
        then(transactionHistoryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("계좌가 존재하고 출금한도에 도달하지 않으면 출금할 수 있다")
    void success_withdrawal() {
        account.deposit(1_000_000L);
        given(accountRepository.findById(any())).willReturn(Optional.of(account));
        given(transactionHistoryRepository.findSumWidrawalDay(any(), any())).willReturn(500_000L);

        accountService.withdrawal(any(), 400_000L);

        assertThat(account.getBalanceAmount()).isEqualTo(600_000L);
        then(accountRepository).should(times(1)).findById(any());
        then(transactionHistoryRepository).should(times(1)).findSumWidrawalDay(any(), any());
        then(transactionHistoryRepository).should(times(1)).save(any());

    }

    @Test
    @DisplayName("출금한도를 초과하면 출금할 수 없다")
    void fail_withdrawal_exceed_amount() {
        account.deposit(1_000_000L);
        given(accountRepository.findById(any())).willReturn(Optional.of(account));
        given(transactionHistoryRepository.findSumWidrawalDay(any(), any())).willReturn(500_000L);

        //when
        assertThatThrownBy(() -> accountService.withdrawal(any(), 600_000L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("exceed limit withdrawal-amount");

        assertThat(account.getBalanceAmount()).isEqualTo(1_000_000L);
        then(accountRepository).should(times(1)).findById(any());
        then(transactionHistoryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("계좌가 존재하지 않으면 출금할 수 없다")
    void fail_withdrawal_not_exists_account() {

        //when
        assertThatThrownBy(() -> accountService.withdrawal(any(), 1000L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("not found account");

        then(accountRepository).should(times(1)).findById(any());
        then(transactionHistoryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("이체한도를 초과하지 않으면 이체할 수 있다")
    void success_transfer() {
        account.deposit(1_000_000L);

        given(accountRepository.findById(1L)).willReturn(Optional.of(account));
        given(accountRepository.findById(2L)).willReturn(Optional.of(depositedAccount));
        given(transactionHistoryRepository.findSumWidrawalDay(any(), any())).willReturn(500_000L);

        //when
        accountService.transfer(1L, 2L, 100_000L);

        assertThat(account.getBalanceAmount()).isEqualTo(899_000L);
        assertThat(depositedAccount.getBalanceAmount()).isEqualTo(100_000L);

        then(accountRepository).should(times(2)).findById(any());
        then(transactionHistoryRepository).should(times(1)).save(any());
    }

    @Test
    @DisplayName("이체한도를 초과하면 이체할 수 없다")
    void fail_transfer_when_exceed_amount() {
        account.deposit(1_000_000L);
        given(accountRepository.findById(1L)).willReturn(Optional.of(account));
        given(accountRepository.findById(2L)).willReturn(Optional.of(depositedAccount));
        given(transactionHistoryRepository.findSumWidrawalDay(any(), any())).willReturn(2_600_000L);

        //when
        assertThatThrownBy(() -> accountService.transfer(1L, 2L, 500_000L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("exceed limit transfer-amount");

        assertThat(account.getBalanceAmount()).isEqualTo(1_000_000L);
        assertThat(depositedAccount.getBalanceAmount()).isEqualTo(0L);

        then(accountRepository).should(times(2)).findById(any());
        then(transactionHistoryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("출금 계좌가 존재하지 않으면 이체할 수 없다")
    void fail_transfer_when_not_exists_withdrawal_account() {
        assertThatThrownBy(() -> accountService.transfer(1L, 2L, 500_000L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("not found transfer-account");

        then(accountRepository).should(times(1)).findById(any());
        then(transactionHistoryRepository).should(never()).save(any());

    }

    @Test
    @DisplayName("입금 계좌가 존재하지 않으면 이체할 수 없다")
    void fail_transfer_when_not_exists_deposit_account() {
        given(accountRepository.findById(1L)).willReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.transfer(1L, 2L, 500_000L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("not found deposit-account");

        then(accountRepository).should(times(2)).findById(any());
        then(transactionHistoryRepository).should(never()).save(any());
    }

    @Spy
    TransactionHistory transactionHistory1;

    @Spy
    TransactionHistory transactionHistory2;

    @Test
    @DisplayName("계좌의 거래 내역을 조회할 수 있다")
    void success_get_transaction_history() {
        given(transactionHistory1.getCreatedAt()).willReturn(LocalDateTime.now().minusDays(1));
        given(transactionHistory2.getCreatedAt()).willReturn(LocalDateTime.now());

        List<TransactionHistory> transactionHistoryList = Arrays.asList(transactionHistory2, transactionHistory1);

        given(accountRepository.findById(any())).willReturn(Optional.of(account));
        given(transactionHistoryRepository.findAllByAccountId(any())).willReturn(transactionHistoryList);

        List<TransactionHistoryDTO> result = accountService.getTransactionHistory(any());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).createdAt()).isAfter(result.get(1).createdAt());

        then(accountRepository).should(times(1)).findById(any());
        then(transactionHistoryRepository).should(times(1)).findAllByAccountId(any());
    }

    @Test
    @DisplayName("계좌가 존재하지 않으면 거래 내역을 조회할 수 없다")
    void fail_get_transaction_history_when_account_not_found() {

        given(accountRepository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getTransactionHistory(any()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("not found account");

        then(accountRepository).should(times(1)).findById(any());
        then(transactionHistoryRepository).should(never()).findAllByAccountId(any());
    }

}
