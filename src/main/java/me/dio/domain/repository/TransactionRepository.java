package me.dio.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.dio.domain.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Statements read newest-first; the entity graph eagerly loads the account
    // so the DTO can render it after the session closes (same pattern as UserRepository).
    @EntityGraph(attributePaths = {"account"})
    Page<Transaction> findByAccount_IdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}
