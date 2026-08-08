package me.dio.service.impl;

import me.dio.controller.dto.TransactionRequestDto;
import me.dio.domain.model.Account;
import me.dio.domain.model.Transaction;
import me.dio.domain.model.TransactionType;
import me.dio.domain.repository.AccountRepository;
import me.dio.domain.repository.TransactionRepository;
import me.dio.domain.repository.TransactionTypeSummary;
import me.dio.service.TransactionService;
import me.dio.service.exception.BusinessException;
import me.dio.service.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findByAccountId(Long accountId, Pageable pageable) {
        // Keep GET and POST consistent: an unknown account is 404, not an empty page.
        this.accountRepository.findById(accountId).orElseThrow(NotFoundException::new);
        return this.transactionRepository.findByAccount_IdOrderByCreatedAtDesc(accountId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionTypeSummary> summarizeByType() {
        return this.transactionRepository.summarizeByType();
    }

    @Override
    @Transactional
    public Transaction create(Long accountId, TransactionRequestDto request) {
        ofNullable(request).orElseThrow(() -> new BusinessException("Transaction must not be null."));
        ofNullable(request.type()).orElseThrow(() -> new BusinessException("Transaction type must not be null."));
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero.");
        }

        Account source = this.accountRepository.findById(accountId)
                .orElseThrow(NotFoundException::new);

        switch (request.type()) {
            case DEPOSIT -> source.setBalance(source.getBalance().add(request.amount()));
            case WITHDRAWAL, PAYMENT -> {
                this.requireFunds(source, request.amount(), request.type());
                source.setBalance(source.getBalance().subtract(request.amount()));
            }
            case TRANSFER -> {
                this.requireFunds(source, request.amount(), request.type());
                Account destination = this.accountRepository
                        .findById(request.destinationAccountId())
                        .orElseThrow(NotFoundException::new);
                if (destination.getId().equals(source.getId())) {
                    throw new BusinessException("Source and destination accounts must differ.");
                }
                source.setBalance(source.getBalance().subtract(request.amount()));
                destination.setBalance(destination.getBalance().add(request.amount()));
            }
        }

        return this.transactionRepository.save(request.toModel(source));
    }

    /**
     * A debit (WITHDRAWAL / PAYMENT / TRANSFER) must not leave the balance
     * negative. Runs before any balance is mutated, so a failure aborts the
     * whole unit with no partial change.
     *
     * ponytail: two concurrent debits can still race past this in-app check —
     * the DB-level upgrade path is a CHECK (balance &gt;= 0) on tb_account.
     */
    private void requireFunds(Account account, BigDecimal amount, TransactionType type) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds for %s.".formatted(type));
        }
    }
}
