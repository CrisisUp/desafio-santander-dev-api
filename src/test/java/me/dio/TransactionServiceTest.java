package me.dio;

import me.dio.controller.dto.TransactionRequestDto;
import me.dio.domain.model.Account;
import me.dio.domain.model.Card;
import me.dio.domain.model.Transaction;
import me.dio.domain.model.TransactionType;
import me.dio.domain.model.User;
import me.dio.domain.repository.AccountRepository;
import me.dio.domain.repository.TransactionRepository;
import me.dio.domain.repository.TransactionTypeSummary;
import me.dio.domain.repository.UserRepository;
import me.dio.service.TransactionService;
import me.dio.service.UserService;
import me.dio.service.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regression tests for the transaction/statement business rules.
 * Each test runs in a transaction that is rolled back, keeping the Flyway seed intact.
 */
@SpringBootTest
@Transactional
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /** The seeded account (user 1 "Devweekerson") starts at balance 624.12. */
    private static final BigDecimal SEED_BALANCE = new BigDecimal("624.12");

    // ---- create: deposit ----

    @Test
    void depositIncreasesBalance() {
        Long accountId = seededAccountId();
        Transaction t = transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.DEPOSIT, new BigDecimal("100.50"), null));

        assertThat(t.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(t.getId()).isNotNull();
        assertThat(accountRepository.findById(accountId).orElseThrow().getBalance())
                .isEqualByComparingTo(SEED_BALANCE.add(new BigDecimal("100.50")));
    }

    // ---- create: withdrawal ----

    @Test
    void withdrawalDecreasesBalance() {
        Long accountId = seededAccountId();
        transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.WITHDRAWAL, new BigDecimal("24.12"), null));

        assertThat(accountRepository.findById(accountId).orElseThrow().getBalance())
                .isEqualByComparingTo(SEED_BALANCE.subtract(new BigDecimal("24.12")));
    }

    @Test
    void withdrawalRejectsInsufficientFunds() {
        Long accountId = seededAccountId();
        BigDecimal before = accountRepository.findById(accountId).orElseThrow().getBalance();

        assertThatThrownBy(() -> transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.WITHDRAWAL, new BigDecimal("999999.00"), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient funds");

        // Balance unchanged after a rejected debit.
        assertThat(accountRepository.findById(accountId).orElseThrow().getBalance())
                .isEqualByComparingTo(before);
    }

    // ---- create: transfer ----

    @Test
    void transferMovesMoneyBetweenAccounts() {
        Long sourceId = seededAccountId();
        User other = userService.create(newUser("transfer-acct", "transfer-card"));
        Long destinationId = other.getAccount().getId();

        Transaction t = transactionService.create(sourceId,
                new TransactionRequestDto(TransactionType.TRANSFER, new BigDecimal("100.00"), destinationId));

        // Returned leg is the DEBIT on the source.
        assertThat(t.getDestinationAccountId()).isEqualTo(destinationId);
        assertThat(t.isCredit()).isFalse();
        assertThat(accountRepository.findById(sourceId).orElseThrow().getBalance())
                .isEqualByComparingTo(SEED_BALANCE.subtract(new BigDecimal("100.00")));
        // newUser seeds the destination with 100.00; the transfer adds 100.00.
        assertThat(accountRepository.findById(destinationId).orElseThrow().getBalance())
                .isEqualByComparingTo(new BigDecimal("200.00"));

        // Double-entry: the destination statement has the CREDIT leg ("De conta #source").
        var destPage = transactionService.findByAccountId(destinationId, PageRequest.of(0, 10));
        assertThat(destPage.getContent())
                .anyMatch(tx -> tx.getType() == TransactionType.TRANSFER && tx.isCredit()
                        && tx.getDestinationAccountId().equals(sourceId)
                        && tx.getAmount().compareTo(new BigDecimal("100.00")) == 0);
        // The source statement has the DEBIT leg ("Para conta #destination").
        var sourcePage = transactionService.findByAccountId(sourceId, PageRequest.of(0, 10));
        assertThat(sourcePage.getContent())
                .anyMatch(tx -> tx.getType() == TransactionType.TRANSFER && !tx.isCredit()
                        && tx.getDestinationAccountId().equals(destinationId));
    }

    @Test
    void transferWorksWhenSourceHasHigherId() {
        // Regression: the ascending-id lock must still pick the real source when
        // its id is HIGHER than the destination's (a previous bug silently debited
        // the destination to itself, moving no money).
        Long destinationId = seededAccountId(); // account id 1 (low)
        User other = userService.create(newUser("transfer-high-src", "transfer-high-card"));
        Long sourceId = other.getAccount().getId(); // higher than 1

        Transaction t = transactionService.create(sourceId,
                new TransactionRequestDto(TransactionType.TRANSFER, new BigDecimal("25.00"), destinationId));

        // The returned leg is owned by the SOURCE, pointing at the destination.
        assertThat(t.getAccount().getId()).isEqualTo(sourceId);
        assertThat(t.getDestinationAccountId()).isEqualTo(destinationId);
        assertThat(t.isCredit()).isFalse();
        // Money actually moved: source down 25, destination up 25.
        assertThat(accountRepository.findById(sourceId).orElseThrow().getBalance())
                .isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(accountRepository.findById(destinationId).orElseThrow().getBalance())
                .isEqualByComparingTo(SEED_BALANCE.add(new BigDecimal("25.00")));
    }

    @Test
    void transferRequiresDestinationAccount() {
        assertThatThrownBy(() -> transactionService.create(seededAccountId(),
                new TransactionRequestDto(TransactionType.TRANSFER, new BigDecimal("10.00"), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("destination");
    }

    @Test
    void transferCountedOnceInAggregate() {
        Long sourceId = seededAccountId();
        User other = userService.create(newUser("transfer-aggr-acct", "transfer-aggr-card"));
        Long destinationId = other.getAccount().getId();

        transactionService.create(sourceId,
                new TransactionRequestDto(TransactionType.TRANSFER, new BigDecimal("30.00"), destinationId));

        // A transfer writes two rows but must be counted once (debit leg only).
        var summaries = transactionService.summarizeByType();
        assertThat(summaries).filteredOn(s -> s.type() == TransactionType.TRANSFER)
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.count()).isEqualTo(1L);
                    assertThat(s.total()).isEqualByComparingTo("30.00");
                });
    }

    @Test
    void transferRejectsSelfDestination() {
        Long accountId = seededAccountId();
        assertThatThrownBy(() -> transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.TRANSFER, new BigDecimal("10.00"), accountId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("must differ");
    }

    @Test
    void transferRejectsInsufficientFunds() {
        Long sourceId = seededAccountId();
        User other = userService.create(newUser("transfer2-acct", "transfer2-card"));

        assertThatThrownBy(() -> transactionService.create(sourceId,
                new TransactionRequestDto(TransactionType.TRANSFER, new BigDecimal("999999.00"), other.getAccount().getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient funds");
    }

    // ---- create: validation ----

    @Test
    void createRejectsNullRequest() {
        assertThatThrownBy(() -> transactionService.create(seededAccountId(), null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void createRejectsNonPositiveAmount() {
        Long accountId = seededAccountId();
        assertThatThrownBy(() -> transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.DEPOSIT, BigDecimal.ZERO, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Amount");
    }

    @Test
    void createThrowsNotFoundForUnknownAccount() {
        assertThatThrownBy(() -> transactionService.create(999L,
                new TransactionRequestDto(TransactionType.DEPOSIT, new BigDecimal("1.00"), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }

    // ---- idempotency ----

    @Test
    void create_withSameKeyReturnsExistingTransaction() {
        Long accountId = seededAccountId();
        BigDecimal before = accountRepository.findById(accountId).orElseThrow().getBalance();

        Transaction first = transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.DEPOSIT, new BigDecimal("25.00"), null), "key-1");
        Transaction retry = transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.DEPOSIT, new BigDecimal("25.00"), null), "key-1");

        // Same transaction returned; the balance was credited only once.
        assertThat(retry.getId()).isEqualTo(first.getId());
        assertThat(accountRepository.findById(accountId).orElseThrow().getBalance())
                .isEqualByComparingTo(before.add(new BigDecimal("25.00")));
    }

    @Test
    void create_withDifferentKeysCreatesBoth() {
        Long accountId = seededAccountId();
        BigDecimal before = accountRepository.findById(accountId).orElseThrow().getBalance();

        transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.DEPOSIT, new BigDecimal("10.00"), null), "key-a");
        transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.DEPOSIT, new BigDecimal("10.00"), null), "key-b");

        assertThat(accountRepository.findById(accountId).orElseThrow().getBalance())
                .isEqualByComparingTo(before.add(new BigDecimal("20.00")));
    }

    @Test
    void create_withoutKeyCreates() {
        Long accountId = seededAccountId();
        BigDecimal before = accountRepository.findById(accountId).orElseThrow().getBalance();

        transactionService.create(accountId,
                new TransactionRequestDto(TransactionType.DEPOSIT, new BigDecimal("7.00"), null));

        assertThat(accountRepository.findById(accountId).orElseThrow().getBalance())
                .isEqualByComparingTo(before.add(new BigDecimal("7.00")));
    }

    @Test
    void create_sameKeyAcrossDifferentAccountsDoesNotCollide() {
        // The UNIQUE is (account_id, idempotency_key): two accounts may reuse a key.
        Long sourceId = seededAccountId();
        User other = userService.create(newUser("idem-other-acct", "idem-other-card"));
        Long otherId = other.getAccount().getId();

        Transaction a = transactionService.create(sourceId,
                new TransactionRequestDto(TransactionType.DEPOSIT, new BigDecimal("5.00"), null), "shared-key");
        Transaction b = transactionService.create(otherId,
                new TransactionRequestDto(TransactionType.DEPOSIT, new BigDecimal("5.00"), null), "shared-key");

        assertThat(a.getId()).isNotEqualTo(b.getId());
    }

    @Test
    void create_sameKeyOnTransferDoesNotDoubleMoveMoney() {
        Long sourceId = seededAccountId();
        User other = userService.create(newUser("idem-transfer-acct", "idem-transfer-card"));
        Long destinationId = other.getAccount().getId();
        BigDecimal sourceBefore = accountRepository.findById(sourceId).orElseThrow().getBalance();

        Transaction first = transactionService.create(sourceId,
                new TransactionRequestDto(TransactionType.TRANSFER, new BigDecimal("30.00"), destinationId), "transfer-key");
        Transaction retry = transactionService.create(sourceId,
                new TransactionRequestDto(TransactionType.TRANSFER, new BigDecimal("30.00"), destinationId), "transfer-key");

        assertThat(retry.getId()).isEqualTo(first.getId());
        assertThat(accountRepository.findById(sourceId).orElseThrow().getBalance())
                .isEqualByComparingTo(sourceBefore.subtract(new BigDecimal("30.00")));
    }

    // ---- aggregate ----

    @Test
    void summarizeByTypeReturnsTotalsFromSeed() {
        var summaries = transactionService.summarizeByType();

        // V4 (account 1) + V6 (accounts 2..41): 41 deposits, 40 payments, 21 withdrawals.
        assertThat(summaries).extracting(TransactionTypeSummary::type)
                .containsExactlyInAnyOrder(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL, TransactionType.PAYMENT);
        assertThat(summaries).extracting(TransactionTypeSummary::count)
                .containsExactlyInAnyOrder(41L, 40L, 21L);

        assertThat(summaries).filteredOn(s -> s.type() == TransactionType.DEPOSIT)
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.total()).isEqualByComparingTo("108820.00");
                    assertThat(s.count()).isEqualTo(41L);
                });
        assertThat(summaries).filteredOn(s -> s.type() == TransactionType.PAYMENT)
                .singleElement()
                .satisfies(s -> assertThat(s.total()).isEqualByComparingTo("15110.00"));
        assertThat(summaries).filteredOn(s -> s.type() == TransactionType.WITHDRAWAL)
                .singleElement()
                .satisfies(s -> assertThat(s.total()).isEqualByComparingTo("7155.88"));

        // TRANSFER has no seed rows → absent from the aggregate (frontend zero-fills).
        assertThat(summaries).extracting(TransactionTypeSummary::type)
                .doesNotContain(TransactionType.TRANSFER);
    }

    // ---- read ----

    @Test
    void findByAccountIdReturnsPagedStatement() {
        Long accountId = seededAccountId();
        // Seed V4 adds 2 transactions for account 1.
        var page = transactionService.findByAccountId(accountId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent()).isNotEmpty();
        // Newest-first ordering.
        assertThat(page.getContent().get(0).getCreatedAt())
                .isAfterOrEqualTo(page.getContent().get(1).getCreatedAt());
    }

    @Test
    void findByAccountIdThrowsNotFoundForUnknownAccount() {
        // Regression: GET on an unknown account used to return an empty page (200);
        // it must now be consistent with POST and return 404.
        assertThatThrownBy(() -> transactionService.findByAccountId(999L, PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }

    // ---- helpers ----

    private Long seededAccountId() {
        return userRepository.findById(1L).orElseThrow().getAccount().getId();
    }

    private User newUser(String accountNumber, String cardNumber) {
        User user = new User();
        user.setName("Test User");
        Account account = new Account();
        account.setNumber(accountNumber);
        account.setAgency("0001");
        account.setBalance(BigDecimal.valueOf(100.00));
        account.setLimit(BigDecimal.valueOf(1000.00));
        user.setAccount(account);
        Card card = new Card();
        card.setNumber(cardNumber);
        card.setLimit(BigDecimal.valueOf(5000.00));
        user.setCard(card);
        user.setFeatures(new ArrayList<>());
        user.setNews(new ArrayList<>());
        return user;
    }
}
