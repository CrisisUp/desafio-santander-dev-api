package me.dio.controller.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import me.dio.domain.model.Account;
import me.dio.domain.model.Transaction;
import me.dio.domain.model.TransactionType;

import java.time.LocalDateTime;

public record TransactionRequestDto(
        @NotNull TransactionType type,
        @NotNull @Positive @Digits(integer = 11, fraction = 2) BigDecimal amount,
        // null = non-transfer; for TRANSFER the service requires a destination.
        @Positive Long destinationAccountId) {

    public Transaction toModel(Account account) {
        Transaction t = new Transaction();
        t.setAccount(account);
        t.setType(this.type);
        t.setAmount(this.amount);
        t.setDestinationAccountId(this.destinationAccountId);
        t.setCreatedAt(LocalDateTime.now());
        // DEPOSIT credits; WITHDRAWAL/PAYMENT/TRANSFER debit. For TRANSFER this is
        // the DEBIT leg (the service also writes a matching credit leg on the
        // destination account).
        t.setCredit(this.type == TransactionType.DEPOSIT);
        return t;
    }
}
