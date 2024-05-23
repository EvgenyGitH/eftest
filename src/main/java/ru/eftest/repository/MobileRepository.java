package ru.eftest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.eftest.model.Mobile;

import java.util.List;


public interface MobileRepository extends JpaRepository<Mobile, Long> {
    boolean existsByNumber(String number);
    List<Mobile> findAllByUserId(Long userId);
}
