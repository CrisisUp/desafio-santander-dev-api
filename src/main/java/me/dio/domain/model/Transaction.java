package me.dio.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "tb_transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(precision = 13, scale = 2, nullable = false)
    private BigDecimal amount;

    // Only set on TRANSFER (nullable otherwise). Stored as a plain FK so the
    // statement can show "para conta X" without a bidirectional mapping.
    // For the credit leg it holds the SOURCE id ("de conta X").
    //
    // ponytail: no DB-level FK on this column (the service validates existence
    // at runtime). Upgrade path: map a real relation to Account once the schema
    // allows it (a new migration, since this one is already applied).
    @Column(name = "destination_account_id")
    private Long destinationAccountId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Idempotency-Key header from the creating request, so a duplicate-click or
    // retry with the same key returns the original transaction instead of
    // debiting twice. UNIQUE (account_id, idempotency_key) in V8.
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    // Direction of the movement. A TRANSFER now writes TWO rows (one per account):
    // the debit leg (credit=false) on the source and the credit leg (credit=true)
    // on the destination. DEPOSITs are credits; WITHDRAWAL/PAYMENT are debits.
    @Column(nullable = false)
    private boolean credit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCredit() {
        return credit;
    }

    public void setCredit(boolean credit) {
        this.credit = credit;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
