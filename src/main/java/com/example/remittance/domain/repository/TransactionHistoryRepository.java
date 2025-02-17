package com.example.remittance.domain.repository;

import com.example.remittance.domain.model.TransactionHistory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    @Query(
            value = "select COALESCE(sum(a.amount), 0) "
            + "from transaction_history as a "
            + "where withdrawal_id = :withdrawalId "
            + " and left(created_at, 10) = left(:stdDate, 10)"
            , nativeQuery = true
    )
    long findSumWidrawalDay(Long withdrawalId, LocalDateTime stdDate);

    @Query(
            value = "select * "
            + "from transaction_history as a "
            + "where (withdrawal_id = :accountId or deposit_id = :accountId) "
            + "order by created_at desc"
            , nativeQuery = true
    )
    List<TransactionHistory> findAllByAccountId(Long accountId);
}
