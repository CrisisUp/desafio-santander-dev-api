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

        assertThat(t.getDestinationAccountId()).isEqualTo(destinationId);
        assertThat(accountRepository.findById(sourceId).orElseThrow().getBalance())
                .isEqualByComparingTo(SEED_BALANCE.subtract(new BigDecimal("100.00")));
        // newUser seeds the destination with 100.00; the transfer adds 100.00.
        assertThat(accountRepository.findById(destinationId).orElseThrow().getBalance())
                .isEqualByComparingTo(new BigDecimal("200.00"));
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
