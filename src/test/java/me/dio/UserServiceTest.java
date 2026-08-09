package me.dio;

import me.dio.domain.model.Account;
import me.dio.domain.model.Card;
import me.dio.domain.model.Feature;
import me.dio.domain.model.User;
import me.dio.domain.repository.UserRepository;
import me.dio.service.UserService;
import me.dio.service.exception.BusinessException;
import me.dio.service.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regression tests for the create/update/delete business rules.
 * Each test runs in a transaction that is rolled back, keeping the Flyway seed intact.
 */
// Isolated in-memory H2: the persistent dev DB (data/sdw2023.mv.db) is locked
// by a running API, so tests must not open it (else "Database already in use").
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ---- create ----

    @Test
    void create_validUserPersists() {
        User created = userService.create(newUser("acct-new", "card-new"));
        assertThat(created.getId()).isNotNull();
        assertThat(userRepository.findById(created.getId())).isPresent();
    }

    @Test
    void create_rejectsBlankName() {
        User user = newUser("acct-bn", "card-bn");
        user.setName("   ");
        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("name");
    }

    @Test
    void create_rejectsNullUser() {
        assertThatThrownBy(() -> userService.create(null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_rejectsNullAccount() {
        User user = newUser("acct-na", "card-na");
        user.setAccount(null);
        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("account");
    }

    @Test
    void create_rejectsNullCard() {
        User user = newUser("acct-nc", "card-nc");
        user.setCard(null);
        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("card");
    }

    @Test
    void create_rejectsProtectedId() {
        User user = newUser("acct-pi", "card-pi");
        user.setId(1L);
        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ID 1");
    }

    @Test
    void create_rejectsDuplicateAccountNumber() {
        userService.create(newUser("dup-acct", "card-1"));
        assertThatThrownBy(() -> userService.create(newUser("dup-acct", "card-2")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void create_rejectsDuplicateCardNumber() {
        userService.create(newUser("acct-1", "dup-card"));
        assertThatThrownBy(() -> userService.create(newUser("acct-2", "dup-card")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void create_rejectsPersistedChildIds() {
        // Regression: a POST with a Feature that already exists (e.g. from the seed)
        // used to fail with a 500 "detached entity passed to persist".
        User user = newUser("acct-ci", "card-ci");
        Feature seedFeature = new Feature();
        seedFeature.setId(1L);
        user.setFeatures(List.of(seedFeature));
        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("must not be set");
    }

    @Test
    void findAll_filtersByNameIgnoringCase() {
        // Create, then rename to a distinctive name so the search has a target.
        User target = userService.create(newUser("find-me-acct", "find-me-card"));
        target.setName("FINDABLE Name");
        userService.update(target.getId(), target);

        var matches = userService.findAll("findable", PageRequest.of(0, 20));
        assertThat(matches.getContent()).anyMatch(u -> u.getName().equals("FINDABLE Name"));
        assertThat(matches.getTotalElements()).isGreaterThanOrEqualTo(1);

        var noMatch = userService.findAll("zzz-nonexistent-zzz", PageRequest.of(0, 20));
        assertThat(noMatch.getTotalElements()).isZero();
    }

    @Test
    void findAll_returnsPaginatedResults() {
        userService.create(newUser("page-1", "page-1"));
        userService.create(newUser("page-2", "page-2"));
        userService.create(newUser("page-3", "page-3"));

        // size=2 with an offset past the seed user (id 1) is not deterministic,
        // so assert on structure instead of exact contents.
        var page = userService.findAll(PageRequest.of(0, 2));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(2);
    }

    // ---- update ----

    @Test
    void update_validUpdatesNameWithoutDuplicatingChildren() {
        User created = userService.create(newUser("acct-upd", "card-upd"));

        User update = newUser("acct-upd", "card-upd");
        update.setId(created.getId());
        update.getAccount().setId(created.getAccount().getId());
        update.getCard().setId(created.getCard().getId());
        update.setName("Updated");

        User result = userService.update(created.getId(), update);

        assertThat(result.getName()).isEqualTo("Updated");
        // The child rows must be updated, not duplicated by cascade = ALL.
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM tb_account WHERE number = ?", Integer.class, "acct-upd"))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM tb_card WHERE number = ?", Integer.class, "card-upd"))
                .isEqualTo(1);
    }

    @Test
    void update_rejectsDuplicateAccountNumberFromAnotherUser() {
        userService.create(newUser("acct-d1", "card-d1"));
        User second = userService.create(newUser("acct-d2", "card-d2"));

        // second steals acct-d1's number -> must fail even though the number is
        // not "new" in the DB.
        User update = newUser("acct-d1", "card-d2");
        update.setId(second.getId());
        update.getAccount().setId(second.getAccount().getId());
        update.getCard().setId(second.getCard().getId());
        assertThatThrownBy(() -> userService.update(second.getId(), update))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("account number already exists");
    }

    @Test
    void update_keepsOwnAccountNumberWithoutFalseDuplicate() {
        // Regression: the uniqueness check on update must exclude the user's own
        // number, otherwise an unchanged update is rejected.
        User created = userService.create(newUser("acct-own", "card-own"));
        User update = newUser("acct-own", "card-own");
        update.setId(created.getId());
        update.getAccount().setId(created.getAccount().getId());
        update.getCard().setId(created.getCard().getId());
        User result = userService.update(created.getId(), update);
        assertThat(result.getName()).isEqualTo("Test User");
    }

    @Test
    void update_rejectsBlankName() {
        User created = userService.create(newUser("acct-bn2", "card-bn2"));
        User update = newUser("acct-bn2", "card-bn2");
        update.setId(created.getId());
        update.getAccount().setId(created.getAccount().getId());
        update.getCard().setId(created.getCard().getId());
        update.setName("   ");
        assertThatThrownBy(() -> userService.update(created.getId(), update))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("name");
    }

    @Test
    void update_rejectsProtectedId() {
        User update = newUser("acct-up", "card-up");
        update.setId(1L);
        update.getAccount().setId(1L);
        update.getCard().setId(1L);
        assertThatThrownBy(() -> userService.update(1L, update))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("can not be updated");
    }

    @Test
    void update_rejectsMismatchedIds() {
        User created = userService.create(newUser("acct-mm", "card-mm"));
        User update = newUser("acct-mm", "card-mm");
        update.setId(created.getId() + 1);
        update.getAccount().setId(created.getAccount().getId());
        update.getCard().setId(created.getCard().getId());
        assertThatThrownBy(() -> userService.update(created.getId(), update))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Update IDs");
    }

    @Test
    void update_rejectsWrongAccountId() {
        User created = userService.create(newUser("acct-wa", "card-wa"));
        User update = newUser("acct-wa", "card-wa");
        update.setId(created.getId());
        update.getAccount().setId(999L);
        update.getCard().setId(created.getCard().getId());
        assertThatThrownBy(() -> userService.update(created.getId(), update))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Account ID");
    }

    @Test
    void update_rejectsFeatureFromAnotherUser() {
        User a = userService.create(newUserWithFeature("acct-fa", "card-fa", "feat-a"));
        User b = userService.create(newUserWithFeature("acct-fb", "card-fb", "feat-b"));
        Long foreignFeatureId = a.getFeatures().get(0).getId();

        User update = newUserWithFeature("acct-fb", "card-fb", "feat-b");
        update.setId(b.getId());
        update.getAccount().setId(b.getAccount().getId());
        update.getCard().setId(b.getCard().getId());
        update.getFeatures().get(0).setId(foreignFeatureId);

        assertThatThrownBy(() -> userService.update(b.getId(), update))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("must belong");
    }

    @Test
    void update_rejectsDuplicateFeatureInSameList() {
        // Regression: the join table has UNIQUE(features_id), so listing the same
        // feature twice in one update would violate it — reject as a clean 422.
        User created = userService.create(newUserWithFeature("acct-df", "card-df", "feat-df"));
        Long featureId = created.getFeatures().get(0).getId();

        User update = newUserWithFeature("acct-df", "card-df", "feat-df");
        update.setId(created.getId());
        update.getAccount().setId(created.getAccount().getId());
        update.getCard().setId(created.getCard().getId());
        update.getFeatures().get(0).setId(featureId);
        update.getFeatures().add(created.getFeatures().get(0)); // same id twice

        assertThatThrownBy(() -> userService.update(created.getId(), update))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void update_rejectsNullFeatures() {
        User created = userService.create(newUser("acct-nf", "card-nf"));
        User update = newUser("acct-nf", "card-nf");
        update.setId(created.getId());
        update.getAccount().setId(created.getAccount().getId());
        update.getCard().setId(created.getCard().getId());
        update.setFeatures(null);
        assertThatThrownBy(() -> userService.update(created.getId(), update))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("must not be null");
    }

    // ---- delete / findById ----

    @Test
    void delete_rejectsProtectedId() {
        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("can not be deleted");
    }

    @Test
    void findById_throwsWhenMissing() {
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    // ---- checkUniqueness ----

    @Test
    void checkUniqueness_returnsAvailableWhenUnused() {
        var result = userService.checkUniqueness("999-unused-account", "999-unused-card", null);
        assertThat(result.accountNumberAvailable()).isTrue();
        assertThat(result.cardNumberAvailable()).isTrue();
    }

    @Test
    void checkUniqueness_reportsTakenForSeedAccountAndCard() {
        // V5 seed: account '0002' and card '**** **** **** 2201' exist.
        var result = userService.checkUniqueness("0002", "**** **** **** 2201", null);
        assertThat(result.accountNumberAvailable()).isFalse();
        assertThat(result.cardNumberAvailable()).isFalse();
    }

    @Test
    void checkUniqueness_ignoresOwnNumbersOnEdit() {
        User created = userService.create(newUser("check-own-acct", "check-own-card"));
        // Editing this user: its own account/card must not be reported as taken.
        var result = userService.checkUniqueness("check-own-acct", "check-own-card", created.getId());
        assertThat(result.accountNumberAvailable()).isTrue();
        assertThat(result.cardNumberAvailable()).isTrue();
        // But ANOTHER user's number is still taken even when editing this one.
        var otherTaken = userService.checkUniqueness("0002", "**** **** **** 2201", created.getId());
        assertThat(otherTaken.accountNumberAvailable()).isFalse();
        assertThat(otherTaken.cardNumberAvailable()).isFalse();
    }

    @Test
    void checkUniqueness_blankValuesAreAvailable() {
        var result = userService.checkUniqueness("  ", null, null);
        assertThat(result.accountNumberAvailable()).isTrue();
        assertThat(result.cardNumberAvailable()).isTrue();
    }

    // ---- helpers ----

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

    private User newUserWithFeature(String accountNumber, String cardNumber, String featureDescription) {
        User user = newUser(accountNumber, cardNumber);
        Feature feature = new Feature();
        feature.setDescription(featureDescription);
        user.setFeatures(new ArrayList<>(List.of(feature)));
        return user;
    }
}
