package com.example.remittance.application.service;

import com.example.remittance.application.annotation.DistributedLock;
import com.example.remittance.application.dto.AccountDTO;
import com.example.remittance.domain.model.Account;
import com.example.remittance.domain.model.TransactionHistory;
import com.example.remittance.domain.model.TransactionType;
import com.example.remittance.application.dto.TransactionHistoryDTO;
import com.example.remittance.domain.repository.AccountRepository;
import com.example.remittance.domain.repository.TransactionHistoryRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    public static final long LIMIT_WITHDRAWAL_AMOUNT = 1_000_000L;
    public static final long LIMIT_TRANSFER_AMOUNT = 3_000_000L;

    @Transactional
    public void createAccount() {
        accountRepository.save(new Account());
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found account"));

        if (account.getBalanceAmount() > 0) {
            throw new RuntimeException("impossible delete account because exists balance-amount");
        }

        accountRepository.delete(account);
    }

    @DistributedLock(keys = {"#id"})
    @Transactional
    public AccountDTO deposit(Long id, Long amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found account"));

        account.deposit(amount);

        TransactionHistory transactionHistory = TransactionHistory.builder()
                .transactionType(TransactionType.DEPOSIT)
                .depositId(account.getId())
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        transactionHistoryRepository.save(transactionHistory);

        return AccountDTO.from(account);
    }

    @DistributedLock(keys = {"#id"})
    @Transactional
    public AccountDTO withdrawal(Long id, Long amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found account"));

        long sumWithdrawalAmount = transactionHistoryRepository.findSumWidrawalDay(id, LocalDateTime.now());

        if (LIMIT_WITHDRAWAL_AMOUNT < sumWithdrawalAmount + amount) {
            throw new RuntimeException("exceed limit withdrawal-amount");
        }

        account.withdrawal(amount);

        TransactionHistory transactionHistory = TransactionHistory.builder()
                .transactionType(TransactionType.WITHDRAWAL)
                .withdrawalId(id)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        transactionHistoryRepository.save(transactionHistory);

        return AccountDTO.from(account);
    }

    @DistributedLock(keys = {"#transferId", "#depositId"})
    @Transactional
    public List<AccountDTO> transfer(Long transferId, Long depositId, Long amount) {

        Account withdrawalAccount = accountRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("not found transfer-account"));

        Account depositAccount = accountRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("not found deposit-account"));

        long sumTransferAmount = transactionHistoryRepository.findSumWidrawalDay(transferId, LocalDateTime.now());

        if (LIMIT_TRANSFER_AMOUNT < sumTransferAmount + amount) {
            throw new RuntimeException("exceed limit transfer-amount");
        }

        withdrawalAccount.transfer(amount);
        depositAccount.deposit(amount);

        TransactionHistory transactionHistory = TransactionHistory.builder()
                .transactionType(TransactionType.TRANSFER)
                .withdrawalId(transferId)
                .depositId(depositId)
                .amount(amount)
                .fee((long) (amount * 0.01))
                .createdAt(LocalDateTime.now())
                .build();

        transactionHistoryRepository.save(transactionHistory);

        return List.of(withdrawalAccount, depositAccount).stream()
                .map(AccountDTO::from)
                .toList();
    }

    public List<TransactionHistoryDTO> getTransactionHistory(Long id) {
        accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found account"));

        List<TransactionHistory> transactionHistoryList = transactionHistoryRepository.findAllByAccountId(id);

        return transactionHistoryList.stream()
                .map(TransactionHistoryDTO::from)
                .sorted(Comparator.comparing(TransactionHistoryDTO::createdAt).reversed())
                .toList();
    }

}
