package com.example.remittance.domain.model;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
       account = new Account();
    }

    @Test
    @DisplayName("계좌에 입금할 수 있다")
    void success_deposit() {
        account.deposit(1000L);

        assertThat(account.getBalanceAmount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("출금 할 금액이 계좌의 잔액보다 작으면 출금 할 수 있다")
    void success_withdrawal_when_balance_greater_than_withdrawal_amount() {
        account.deposit(1000L);

        account.withdrawal(300L);

        assertThat(account.getBalanceAmount()).isEqualTo(700L);
    }

    @Test
    @DisplayName("출금 할 금액이 계좌의 잔액보다 크면 출금 할 수 없다")
    void fail_withdrawal_when_balance_less_than_withdrawal_amount() {

        assertThatThrownBy(() -> account.withdrawal(1000L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("impossible withdrawal amount is greater than balance amount");
    }

    @Test
    @DisplayName("이체 할 금액과 수수료가 계좌의 잔액보다 작으면 이체 할 수 있다")
    void success_transfer_when_balance_greater_than_transfer_amount() {
        account.deposit(1000L);

        account.transfer(300L);

        assertThat(account.getBalanceAmount()).isEqualTo(697L);
    }

    @Test
    @DisplayName("이체 할 금액과 수수료가 계좌의 잔액보다 크면 이체 할 수 없다")
    void fail_withdrawal_when_balance_less_than_transfer_amount() {
        account.deposit(1000L);

        assertThatThrownBy(() -> account.transfer(1000L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("impossible transfer amount is greater than balance amount");
    }


}
