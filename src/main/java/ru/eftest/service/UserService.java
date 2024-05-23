package ru.eftest.service;

import ru.eftest.dto.NewUser;
import ru.eftest.dto.UpdateUser;
import ru.eftest.dto.UserDto;
import ru.eftest.model.Payment;

import java.time.LocalDate;
import java.util.List;

public interface UserService {
    UserDto createUser(NewUser newUser);

    UserDto updateUser(Long userId, UpdateUser updateUser);

    UserDto getUserById(Long userId);

    void deleteUserById(Long userId);

    List<UserDto> getUsers(String name, LocalDate birthday, String number, String email, int from, int size);

    UserDto sendAmount(Long userId, Payment payment);
}
