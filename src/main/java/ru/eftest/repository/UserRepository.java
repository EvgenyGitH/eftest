package ru.eftest.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.eftest.model.User;

import java.time.LocalDate;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLoginContainingIgnoreCase(String login);

    @Query(value = "select u.id, u.name, u.birthday, u.login, u.password " +
            "from users as u " +
            "left join mobiles as m on u.id = m.user_id " +
            "left join emails as e on u.id = e.user_id " +
            "where ((lower(u.name) like lower (concat('%', ?1, '%'))) or ?1 is null) " +
            "and (u.birthday >= cast(?2 as timestamp) or cast(?2 as timestamp) is null) " +
            "and (m.number = ?3 or ?3 is null) " +
            "and (e.email = ?4 or ?4 is null) " +
            "order by u.id asc", nativeQuery = true
    )
    List<User> getUsers(String name, LocalDate birthday, String number, String email, Pageable pageable);


}
