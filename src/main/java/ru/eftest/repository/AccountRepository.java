package ru.eftest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.eftest.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUserId(Long userId);

}
