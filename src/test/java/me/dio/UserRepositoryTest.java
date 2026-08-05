package me.dio;

import me.dio.domain.model.User;
import me.dio.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllFetchesWholeAggregateWithoutLazyInitialization() {
        // The Flyway seed creates the user with id=1 (name "Devweekerson").
        User user = userRepository.findAll().stream()
                .filter(u -> u.getId() == 1L)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Seeded user id=1 not found"));

        // Accessing the graph after the session is closed proves eager graph fetching.
        assertThat(user.getName()).isEqualTo("Devweekerson");
        assertThat(user.getAccount()).isNotNull();
        assertThat(user.getCard()).isNotNull();
        assertThat(user.getFeatures()).hasSize(5);
        assertThat(user.getNews()).hasSize(2);
    }
}
