package ru.eftest.mapper;

import ru.eftest.dto.NewUser;
import ru.eftest.dto.UserDto;
import ru.eftest.model.Email;
import ru.eftest.model.Mobile;
import ru.eftest.model.User;

import java.util.List;

public class UserMapper {
    public static User toUser(NewUser newUser) {
        User user = new User();
        user.setName(newUser.getName());
        user.setBirthday(newUser.getBirthday());
        user.setLogin(newUser.getLogin());
        user.setPassword(newUser.getPassword());
        return user;
    }

    public static UserDto toDto(User user, List<Mobile> mobiles, List<Email> emails, double amount) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setBirthday(user.getBirthday());
        userDto.setLogin(user.getLogin());
        userDto.setMobile(mobiles);
        userDto.setEmail(emails);
        userDto.setAmount(amount);
        return userDto;
    }

}
