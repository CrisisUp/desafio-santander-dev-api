package me.dio.service;

import me.dio.controller.dto.TransactionRequestDto;
import me.dio.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);
    Transaction create(Long accountId, TransactionRequestDto request);
}
