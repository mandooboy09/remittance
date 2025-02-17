package com.example.remittance.presentation.controller;

import com.example.remittance.application.dto.AccountDTO;
import com.example.remittance.application.dto.TransactionHistoryDTO;
import com.example.remittance.application.service.AccountService;

import com.example.remittance.presentation.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ApiResponse<?> createAccount() {
        accountService.createAccount();

        return ApiResponse.of(HttpStatus.OK, "account creation is success");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);

        return ApiResponse.of(HttpStatus.OK, "account deletion is success");
    }

    @PatchMapping("{id}/deposit/{amount}")
    public ApiResponse<?> deposit(@PathVariable Long id, @PathVariable Long amount) {
        AccountDTO accountDTO = accountService.deposit(id, amount);

        return ApiResponse.of(HttpStatus.OK, "deposit is success", accountDTO);
    }

    @PatchMapping("{id}/withdrawal/{amount}")
    public ApiResponse<?> withdrawal(@PathVariable Long id, @PathVariable Long amount) {
        AccountDTO accountDTO = accountService.withdrawal(id, amount);

        return ApiResponse.of(HttpStatus.OK, "withdrawal is success", accountDTO);
    }

    @PatchMapping("{transferId}/transfer/{depositId}/{amount}")
    public ApiResponse<?> transfer(@PathVariable Long transferId, @PathVariable Long depositId, @PathVariable Long amount) {
        List<AccountDTO> accountDTOList = accountService.transfer(transferId, depositId, amount);

        return ApiResponse.of(HttpStatus.OK, "transfer is success", accountDTOList);
    }

    @GetMapping("{id}/transaction-history")
    public ApiResponse<?> transactionHistory(@PathVariable Long id) {
        List<TransactionHistoryDTO> transactionHistoryDTOList = accountService.getTransactionHistory(id);

        return ApiResponse.of(HttpStatus.OK, "transaction-history is success", transactionHistoryDTOList);
    }

}
