package me.dio.service.impl;

import me.dio.controller.dto.TransactionRequestDto;
import me.dio.domain.model.Account;
import me.dio.domain.model.Transaction;
import me.dio.domain.model.TransactionType;
import me.dio.domain.repository.AccountRepository;
import me.dio.domain.repository.TransactionRepository;
import me.dio.domain.repository.TransactionTypeSummary;
import me.dio.service.AuditService;
import me.dio.service.TransactionService;
import me.dio.service.exception.BusinessException;
import me.dio.service.exception.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountRepository accountRepository,
                                  AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
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
        return this.create(accountId, request, null);
    }

    @Override
    @Transactional
    public Transaction create(Long accountId, TransactionRequestDto request, String idempotencyKey) {
        ofNullable(request).orElseThrow(() -> new BusinessException("Transaction must not be null."));
        ofNullable(request.type()).orElseThrow(() -> new BusinessException("Transaction type must not be null."));
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero.");
        }
        if (request.type() == TransactionType.TRANSFER && request.destinationAccountId() == null) {
            throw new BusinessException("Transfer requires a destination account.");
        }

        // Idempotency: a retry with the same key returns the original transaction
        // instead of debiting twice. Backed by UNIQUE (account_id, idempotency_key).
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = this.transactionRepository.findByAccount_IdAndIdempotencyKey(accountId, idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        try {
            return switch (request.type()) {
                case DEPOSIT -> {
                    // Locked for consistency with debits (a deposit concurrent with a
                    // transfer on the same account must not see a stale balance).
                    Account source = this.lockAccount(accountId);
                    source.setBalance(source.getBalance().add(request.amount()));
                    Transaction saved = this.saveWithKey(request.toModel(source), idempotencyKey);
                    this.auditService.log("CREATE_TRANSACTION", null, "system",
                            "tb_transaction", saved.getId(),
                            "{\"type\":\"DEPOSIT\",\"amount\":" + request.amount() + ",\"accountId\":" + accountId + "}");
                    yield saved;
                }
                case WITHDRAWAL, PAYMENT -> {
                    Account source = this.lockAccount(accountId);
                    this.requireFunds(source, request.amount(), request.type());
                    source.setBalance(source.getBalance().subtract(request.amount()));
                    Transaction saved = this.saveWithKey(request.toModel(source), idempotencyKey);
                    this.auditService.log("CREATE_TRANSACTION", null, "system",
                            "tb_transaction", saved.getId(),
                            "{\"type\":\"" + request.type() + "\",\"amount\":" + request.amount() + ",\"accountId\":" + accountId + "}");
                    yield saved;
                }
                case TRANSFER -> this.doTransfer(accountId, request, idempotencyKey);
            };
        } catch (DataIntegrityViolationException race) {
            // Two concurrent retries with the same key: the second hit the UNIQUE
            // constraint. Re-fetch and return the winner instead of a 500/422.
            return this.transactionRepository.findByAccount_IdAndIdempotencyKey(accountId, idempotencyKey)
                    .orElseThrow(() -> race);
        }
    }

    private Transaction saveWithKey(Transaction tx, String idempotencyKey) {
        tx.setIdempotencyKey(idempotencyKey);
        return this.transactionRepository.save(tx);
    }

    /**
     * A transfer debits the source and credits the destination, writing ONE
     * transaction row per account in the same unit:
     *   - debit leg  (credit=false, on the source): "Para conta #X"
     *   - credit leg (credit=true,  on the destination): "De conta #X"
     * Both accounts are locked in ascending id order (PESSIMISTIC_WRITE) so two
     * crossed transfers cannot deadlock, and two concurrent debits on the same
     * source cannot both pass the funds check. Returns the debit leg.
     */
    private Transaction doTransfer(Long accountId, TransactionRequestDto request, String idempotencyKey) {
        Long destinationId = request.destinationAccountId();
        if (destinationId.equals(accountId)) {
            throw new BusinessException("Source and destination accounts must differ.");
        }

        // Lock in ascending id order to avoid deadlocks on crossed transfers,
        // then map source/destination by id comparison — never by object identity
        // (a regression here silently debits the destination to itself).
        Long lowId = Math.min(accountId, destinationId);
        Long highId = Math.max(accountId, destinationId);
        Account low = this.lockAccount(lowId);
        Account high = this.lockAccount(highId);

        Account source = low.getId().equals(accountId) ? low : high;
        Account destination = source == low ? high : low;

        this.requireFunds(source, request.amount(), TransactionType.TRANSFER);

        source.setBalance(source.getBalance().subtract(request.amount()));
        destination.setBalance(destination.getBalance().add(request.amount()));

        LocalDateTime now = LocalDateTime.now();

        Transaction debit = new Transaction();
        debit.setAccount(source);
        debit.setType(TransactionType.TRANSFER);
        debit.setAmount(request.amount());
        debit.setDestinationAccountId(destination.getId());
        debit.setCreatedAt(now);
        debit.setCredit(false);
        debit.setIdempotencyKey(idempotencyKey);
        this.transactionRepository.save(debit);

        Transaction credit = new Transaction();
        credit.setAccount(destination);
        credit.setType(TransactionType.TRANSFER);
        credit.setAmount(request.amount());
        credit.setDestinationAccountId(source.getId());
        credit.setCreatedAt(now);
        credit.setCredit(true);
        // Same key on the credit leg so a retry scoped to the destination also
        // dedupes; the UNIQUE constraint is per account, so no collision.
        credit.setIdempotencyKey(idempotencyKey);
        this.transactionRepository.save(credit);

        // Audit the transfer (both legs in one entry)
        this.auditService.log("TRANSFER", null, "system",
                "tb_transaction", debit.getId(),
                "{\"sourceAccountId\":" + source.getId() +
                ",\"destinationAccountId\":" + destination.getId() +
                ",\"amount\":" + request.amount() +
                ",\"debitTransactionId\":" + debit.getId() +
                ",\"creditTransactionId\":" + credit.getId() + "}");

        return debit;
    }

    private Account lockAccount(Long id) {
        return this.accountRepository.findByIdForUpdate(id).orElseThrow(NotFoundException::new);
    }

    /**
     * A debit (WITHDRAWAL / PAYMENT / TRANSFER) must not leave the available
     * balance (balance + additional_limit) negative. Runs before any balance is
     * mutated, so a failure aborts the whole unit with no partial change. The
     * calling code holds the account's pessimistic lock, so the balance read
     * here is the latest committed one.
     */
    private void requireFunds(Account account, BigDecimal amount, TransactionType type) {
        BigDecimal available = account.getBalance().add(account.getLimit() != null ? account.getLimit() : BigDecimal.ZERO);
        if (available.compareTo(amount) < 0) {
            throw new BusinessException("Insufficient funds for %s.".formatted(type));
        }
    }
}
