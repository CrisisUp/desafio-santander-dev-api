package me.dio.service;

import me.dio.controller.dto.TransactionRequestDto;
import me.dio.domain.model.Transaction;
import me.dio.domain.repository.TransactionTypeSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);
    Transaction create(Long accountId, TransactionRequestDto request);
    List<TransactionTypeSummary> summarizeByType();
}
