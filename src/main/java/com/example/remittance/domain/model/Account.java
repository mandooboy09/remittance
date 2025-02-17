package com.example.remittance.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long balanceAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Account() {
        LocalDateTime nowAt = LocalDateTime.now();

        this.createdAt = nowAt;
        this.updatedAt = nowAt;
    }

    public void deposit(Long amount) {
        this.balanceAmount += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void withdrawal(Long amount) {
        verifyPossibleWithdrawal(amount);

        this.balanceAmount -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    private void verifyPossibleWithdrawal(Long amount) {
        if (this.balanceAmount < amount) {
            throw new RuntimeException("impossible withdrawal amount is greater than balance amount");
        }
    }

    public void transfer(Long amount) {
        verifyPossibleTransfer(amount);

        this.balanceAmount -= amount + (long)(amount * 0.01);
        this.updatedAt = LocalDateTime.now();
    }

    private void verifyPossibleTransfer(Long amount) {
        if (this.balanceAmount < amount + (long) amount * 0.01) {
            throw new RuntimeException("impossible transfer amount is greater than balance amount");
        }
    }

}
