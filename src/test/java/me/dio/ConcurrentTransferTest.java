package me.dio;

import me.dio.controller.dto.TransactionRequestDto;
import me.dio.domain.model.Account;
import me.dio.domain.model.Card;
import me.dio.domain.model.Transaction;
import me.dio.domain.model.TransactionType;
import me.dio.domain.model.User;
import me.dio.domain.repository.AccountRepository;
import me.dio.domain.repository.UserRepository;
import me.dio.service.TransactionService;
import me.dio.service.UserService;
import me.dio.service.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Two concurrent transfers on the same source must not both pass the funds
 * check. The pessimistic lock (SELECT ... FOR UPDATE) serializes them, so the
 * money-conservation invariant always holds: exactly one succeeds and no money
 * is created.
 *
 * No @Transactional here: each thread commits a real transaction, so the lock
 * actually engages. @AfterEach deletes the users created by each test.
 */
@SpringBootTest
class ConcurrentTransferTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** Source with balance 100; two transfers of 70 each — only one can fit. */
    @Test
    void concurrentTransfersCannotOverdraw() throws Exception {
        User sourceUser = userService.create(newUser("conc-source", "conc-source-card", "100.00"));
        User destUser = userService.create(newUser("conc-dest", "conc-dest-card", "0.00"));
        Long sourceId = sourceUser.getAccount().getId();
        Long destinationId = destUser.getAccount().getId();

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        AtomicInteger successes = new AtomicInteger();
        List<Throwable> failures = new ArrayList<>();

        Runnable attempt = () -> {
            try {
                start.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                failures.add(e);
                done.countDown();
                return;
            }
            try {
                transactionService.create(sourceId,
                        new TransactionRequestDto(TransactionType.TRANSFER, new BigDecimal("70.00"), destinationId));
                successes.incrementAndGet();
            } catch (BusinessException ok) {
                // Insufficient funds for the second one — expected.
            } catch (Throwable t) {
                failures.add(t);
            } finally {
                done.countDown();
            }
        };

        Thread t1 = new Thread(attempt);
        Thread t2 = new Thread(attempt);
        t1.start();
        t2.start();
        start.countDown();
        assertThat(done.await(30, TimeUnit.SECONDS)).as("both threads finished").isTrue();

        assertThat(failures).as("no unexpected failures").isEmpty();
        assertThat(successes.get()).as("exactly one transfer succeeds").isEqualTo(1);

        // Invariant: no money created. Source ends at 30, destination at 70.
        assertThat(accountRepository.findById(sourceId).orElseThrow().getBalance())
                .isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(accountRepository.findById(destinationId).orElseThrow().getBalance())
                .isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @AfterEach
    void cleanup() {
        // Delete ONLY the users this test created (account numbers start with
        // "conc-"). Never touch seed users (2..41) or the protected id=1 seed.
        List<User> created = userRepository.findAll().stream()
                .filter(u -> u.getAccount().getNumber().startsWith("conc-"))
                .toList();
        List<Long> accountIds = created.stream().map(u -> u.getAccount().getId()).toList();
        if (!accountIds.isEmpty()) {
            // Delete transactions before their accounts (FK order): this test
            // commits real transfer rows, so account deletion alone would violate the FK.
            String placeholders = String.join(",", accountIds.stream().map(id -> "?").toList());
            jdbcTemplate.update("DELETE FROM tb_transaction WHERE account_id IN (" + placeholders + ")", accountIds.toArray());
        }
        created.stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .forEach(u -> userService.delete(u.getId()));
    }

    private User newUser(String accountNumber, String cardNumber, String balance) {
        User user = new User();
        user.setName("Concurrent User");
        Account account = new Account();
        account.setNumber(accountNumber);
        account.setAgency("0001");
        account.setBalance(new BigDecimal(balance));
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
