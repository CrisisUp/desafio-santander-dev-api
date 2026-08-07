package me.dio.controller.dto;

import java.math.BigDecimal;

import me.dio.domain.model.Account;
import me.dio.domain.model.Transaction;
import me.dio.domain.model.TransactionType;

import java.time.LocalDateTime;

public record TransactionRequestDto(TransactionType type, BigDecimal amount, Long destinationAccountId) {

    public Transaction toModel(Account account) {
        Transaction t = new Transaction();
        t.setAccount(account);
        t.setType(this.type);
        t.setAmount(this.amount);
        t.setDestinationAccountId(this.destinationAccountId);
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }
}
