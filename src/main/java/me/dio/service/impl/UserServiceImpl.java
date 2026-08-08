package me.dio.service.impl;

import me.dio.controller.dto.UniquenessCheckDto;
import me.dio.domain.model.Feature;
import me.dio.domain.model.News;
import me.dio.domain.model.User;
import me.dio.domain.repository.UserRepository;
import me.dio.service.UserService;
import me.dio.service.exception.BusinessException;
import me.dio.service.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Service
public class UserServiceImpl implements UserService {

    /**
     * ID de usuário utilizado na Santander Dev Week 2023.
     * Por isso, vamos criar algumas regras para mantê-lo integro.
     */
    private static final Long UNCHANGEABLE_USER_ID = 1L;

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return this.findAll(pageable);
        }
        return this.userRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return this.userRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public User create(User userToCreate) {
        ofNullable(userToCreate).orElseThrow(() -> new BusinessException("User to create must not be null."));
        ofNullable(userToCreate.getName()).filter(name -> !name.isBlank())
                .orElseThrow(() -> new BusinessException("User name must not be blank."));
        ofNullable(userToCreate.getAccount()).orElseThrow(() -> new BusinessException("User account must not be null."));
        ofNullable(userToCreate.getCard()).orElseThrow(() -> new BusinessException("User card must not be null."));

        this.validateChangeableId(userToCreate.getId(), "created");
        if (userRepository.existsByAccountNumber(userToCreate.getAccount().getNumber())) {
            throw new BusinessException("This account number already exists.");
        }
        if (userRepository.existsByCardNumber(userToCreate.getCard().getNumber())) {
            throw new BusinessException("This card number already exists.");
        }

        // A new user must bring new children. Pre-persisted IDs (e.g. from the seed)
        // are detached entities and cascade = ALL would fail with a 500 on save.
        requireNewChild(userToCreate.getAccount().getId(), "Account");
        requireNewChild(userToCreate.getCard().getId(), "Card");
        if (userToCreate.getFeatures() != null) {
            userToCreate.getFeatures().forEach(f -> requireNewChild(f.getId(), "Feature"));
        }
        if (userToCreate.getNews() != null) {
            userToCreate.getNews().forEach(n -> requireNewChild(n.getId(), "News"));
        }

        return this.userRepository.save(userToCreate);
    }

    @Override
    @Transactional
    public User update(Long id, User userToUpdate) {
        User dbUser = this.findById(id);
        if (!dbUser.getId().equals(userToUpdate.getId())) {
            throw new BusinessException("Update IDs must be the same.");
        }

        ofNullable(userToUpdate.getName()).filter(name -> !name.isBlank())
                .orElseThrow(() -> new BusinessException("User name must not be blank."));

        // A concurrent update or a blank re-POST colliding with another user's
        // account/card number is surfaced as a clean 422 instead of a 500.
        // The user's own number is excluded so an unchanged update still passes.
        if (userRepository.existsByAccountNumberAndIdNot(userToUpdate.getAccount().getNumber(), dbUser.getId())) {
            throw new BusinessException("This account number already exists.");
        }
        if (userRepository.existsByCardNumberAndIdNot(userToUpdate.getCard().getNumber(), dbUser.getId())) {
            throw new BusinessException("This card number already exists.");
        }

        // Child IDs must match the persisted ones, otherwise cascade = ALL would
        // INSERT new Account/Card/Feature/News rows instead of updating them.
        // Runs before validateChangeableId so a protected user with invalid payload
        // reports the payload problem, not the generic "ID 1" one.
        ofNullable(userToUpdate.getAccount())
                .filter(a -> a.getId() != null && a.getId().equals(dbUser.getAccount().getId()))
                .orElseThrow(() -> new BusinessException("Account ID must match the existing account."));
        ofNullable(userToUpdate.getCard())
                .filter(c -> c.getId() != null && c.getId().equals(dbUser.getCard().getId()))
                .orElseThrow(() -> new BusinessException("Card ID must match the existing card."));
        validateChildCollection(userToUpdate.getFeatures(), "Feature",
                dbUser.getFeatures(), Feature::getId);
        validateChildCollection(userToUpdate.getNews(), "News",
                dbUser.getNews(), News::getId);

        this.validateChangeableId(id, "updated");

        dbUser.setName(userToUpdate.getName());
        dbUser.setAccount(userToUpdate.getAccount());
        dbUser.setCard(userToUpdate.getCard());
        dbUser.setFeatures(userToUpdate.getFeatures());
        dbUser.setNews(userToUpdate.getNews());

        return this.userRepository.save(dbUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        this.validateChangeableId(id, "deleted");
        User dbUser = this.findById(id);
        this.userRepository.delete(dbUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UniquenessCheckDto checkUniqueness(String accountNumber, String cardNumber, Long excludeId) {
        // Blank values are "available": the form only calls with filled fields,
        // and the create/update flow enforces requiredness separately.
        boolean accountAvailable = accountNumber == null || accountNumber.isBlank()
                || (excludeId == null
                        ? !this.userRepository.existsByAccountNumber(accountNumber)
                        : !this.userRepository.existsByAccountNumberAndIdNot(accountNumber, excludeId));
        boolean cardAvailable = cardNumber == null || cardNumber.isBlank()
                || (excludeId == null
                        ? !this.userRepository.existsByCardNumber(cardNumber)
                        : !this.userRepository.existsByCardNumberAndIdNot(cardNumber, excludeId));
        return new UniquenessCheckDto(accountAvailable, cardAvailable);
    }

    /**
     * A child collection on update must be present and its items must belong to the
     * user's persisted collection, otherwise cascade = ALL would INSERT new rows or
     * attach another user's Feature/News to this user.
     */
    private <T> void validateChildCollection(List<T> incoming, String type,
                                             List<T> persisted, Function<T, Long> idGetter) {
        if (incoming == null) {
            throw new BusinessException("%s list must not be null.".formatted(type));
        }
        var persistedIds = persisted.stream().map(idGetter).collect(java.util.stream.Collectors.toSet());
        var seen = new java.util.HashSet<Long>();
        for (T item : incoming) {
            Long id = idGetter.apply(item);
            if (id == null || !persistedIds.contains(id)) {
                throw new BusinessException("%s ID must belong to this user.".formatted(type));
            }
            // The join table has UNIQUE(features_id/news_id); a duplicate in the
            // same list would violate it — reject here as a clean 422.
            if (!seen.add(id)) {
                throw new BusinessException("Duplicate %s ID %d in the same list.".formatted(type, id));
            }
        }
    }

    private void requireNewChild(Long id, String type) {
        if (id != null) {
            throw new BusinessException("%s ID must not be set on a new user.".formatted(type));
        }
    }

    private void validateChangeableId(Long id, String operation) {
        if (UNCHANGEABLE_USER_ID.equals(id)) {
            throw new BusinessException("User with ID %d can not be %s.".formatted(UNCHANGEABLE_USER_ID, operation));
        }
    }
}

