package me.dio.domain.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import me.dio.domain.model.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // SELECT ... FOR UPDATE: serializes concurrent debits/transfers on the same
    // account, so the in-app requireFunds check reads the latest committed balance.
    // Works on H2 and PostgreSQL. Always lock source+destination in ascending id
    // order to avoid deadlocks on crossed transfers.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from tb_account a where a.id = :id")
    Optional<Account> findByIdForUpdate(Long id);
}
