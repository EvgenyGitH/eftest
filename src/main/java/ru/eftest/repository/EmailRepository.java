package ru.eftest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.eftest.model.Email;

import java.util.List;

public interface EmailRepository extends JpaRepository<Email, Long> {
    boolean existsByEmailContainingIgnoreCase(String email);

    List<Email> findAllByUserId(Long userId);
}
