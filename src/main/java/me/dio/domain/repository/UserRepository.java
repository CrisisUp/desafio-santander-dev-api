package me.dio.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.dio.domain.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByAccountNumber(String number);

    boolean existsByCardNumber(String number);

    // EAGER -> LAZY: each read eagerly fetches the full aggregate in a single query
    // instead of N+1 queries (two indexed lists can be fetched in one join).
    @Override
    @EntityGraph(attributePaths = {"account", "card", "features", "news"})
    List<User> findAll();

    // Paged reads also fetch the whole aggregate, one query per page.
    @Override
    @EntityGraph(attributePaths = {"account", "card", "features", "news"})
    Page<User> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"account", "card", "features", "news"})
    Optional<User> findById(Long id);
}
