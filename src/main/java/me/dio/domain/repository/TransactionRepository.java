package me.dio.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.dio.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Statements read newest-first; the entity graph eagerly loads the account
    // so the DTO can render it after the session closes (same pattern as UserRepository).
    @EntityGraph(attributePaths = {"account"})
    Page<Transaction> findByAccount_IdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    // Idempotency: a retry with the same key returns the already-created transaction.
    Optional<Transaction> findByAccount_IdAndIdempotencyKey(Long accountId, String idempotencyKey);

    // Whole-system aggregate: SUM(amount) and COUNT per transaction type.
    // Types with zero rows (e.g. TRANSFER in the seed) produce no entry — the
    // frontend zero-fills the fixed slot list.
    // Each TRANSFER writes two rows (debit + credit legs); count it only once,
    // via the debit leg, so the totals don't double-count a transfer.
    @Query("SELECT new me.dio.domain.repository.TransactionTypeSummary(t.type, SUM(t.amount), COUNT(t)) "
            + "FROM tb_transaction t WHERE (t.type <> me.dio.domain.model.TransactionType.TRANSFER OR t.credit = false) "
            + "GROUP BY t.type")
    List<TransactionTypeSummary> summarizeByType();
}
