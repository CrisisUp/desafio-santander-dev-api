package me.dio.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import me.dio.domain.model.Transaction;
import me.dio.domain.model.TransactionType;

public record TransactionDto(Long id, TransactionType type, BigDecimal amount,
                             Long accountId, Long destinationAccountId, LocalDateTime createdAt,
                             boolean credit) {

    public TransactionDto(Transaction t) {
        this(t.getId(), t.getType(), t.getAmount(),
                t.getAccount() == null ? null : t.getAccount().getId(),
                t.getDestinationAccountId(), t.getCreatedAt(), t.isCredit());
    }
}
