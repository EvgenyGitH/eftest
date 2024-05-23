package ru.eftest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.eftest.dto.NewUser;
import ru.eftest.dto.UpdateUser;
import ru.eftest.dto.UserDto;
import ru.eftest.exception.DataConflictException;
import ru.eftest.exception.NotFoundException;
import ru.eftest.mapper.UserMapper;
import ru.eftest.model.*;
import ru.eftest.repository.AccountRepository;
import ru.eftest.repository.EmailRepository;
import ru.eftest.repository.MobileRepository;
import ru.eftest.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MobileRepository mobileRepository;
    private final EmailRepository emailRepository;
    private final AccountRepository accountRepository;
    private final ScheduledExecutorService scheduler;

    @Override
    public UserDto createUser(NewUser newUser) {
        if (userRepository.existsByLoginContainingIgnoreCase(newUser.getLogin())) {
            throw new DataConflictException("Login is used");
        }
        if (mobileRepository.existsByNumber(newUser.getMobile())) {
            throw new DataConflictException("Mobile is used");
        }
        if (emailRepository.existsByEmailContainingIgnoreCase(newUser.getEmail())) {
            throw new DataConflictException("Email is used");
        }

        User user = UserMapper.toUser(newUser);
        userRepository.save(user);

        Mobile mobile = Mobile.builder()
                .userId(user.getId())
                .number(newUser.getMobile())
                .build();
        mobileRepository.save(mobile);

        Email email = Email.builder()
                .userId(user.getId())
                .email(newUser.getEmail())
                .build();
        emailRepository.save(email);

        Account account = Account.builder()
                .userId(user.getId())
                .amount(newUser.getAmount())
                .updateAmountTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
        accountRepository.save(account);

        List<Mobile> mobiles = mobileRepository.findAllByUserId(user.getId());
        List<Email> emails = emailRepository.findAllByUserId(user.getId());
        updateBalance(user.getId());
        return UserMapper.toDto(user, mobiles, emails, account.getAmount());
    }

    @Override
    public UserDto updateUser(Long userId, UpdateUser updateUser) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User ID: " + userId + " not found"));
        switch (updateUser.getAction()) {
            case ADD:
                if (updateUser.getMobile() != null) {
                    if (mobileRepository.existsByNumber(updateUser.getMobile())) {
                        throw new DataConflictException("Mobile is used");
                    }
                    Mobile mobile = Mobile.builder()
                            .userId(userId)
                            .number(updateUser.getMobile())
                            .build();
                    mobileRepository.save(mobile);
                }
                if (updateUser.getEmail() != null) {
                    if (emailRepository.existsByEmailContainingIgnoreCase(updateUser.getEmail())) {
                        throw new DataConflictException("Email is used");
                    }
                    Email email = Email.builder()
                            .userId(userId)
                            .email(updateUser.getEmail())
                            .build();
                    emailRepository.save(email);
                }
                break;
            case DELETE:
                if (updateUser.getMobile() != null) {
                    List<Mobile> mobilesList = mobileRepository.findAllByUserId(userId);
                    if (mobilesList.size() > 1) {
                        List<Mobile> filterMobiles = mobilesList.stream()
                                .filter(m -> m.getNumber().equals(updateUser.getMobile()))
                                .collect(Collectors.toList());
                        if (filterMobiles.isEmpty()) {
                            throw new NotFoundException("Mobile number: " + updateUser.getMobile() + " not found");
                        }
                        mobileRepository.deleteById(filterMobiles.get(0).getId());

                    } else {
                        throw new DataConflictException("Can't delete. Mobile number less 1");
                    }
                }
                if (updateUser.getEmail() != null) {
                    List<Email> emailsList = emailRepository.findAllByUserId(userId);
                    if (emailsList.size() > 1) {
                        List<Email> filterEmails = emailsList.stream()
                                .filter(e -> e.getEmail().equals(updateUser.getEmail()))
                                .collect(Collectors.toList());
                        if (filterEmails.isEmpty()) {
                            throw new NotFoundException("Email: " + updateUser.getEmail() + " not found");
                        }
                        emailRepository.deleteById(filterEmails.get(0).getId());

                    } else {
                        throw new DataConflictException("Can't delete. Email less 1");
                    }
                }
                break;
        }
        List<Mobile> mobiles = mobileRepository.findAllByUserId(userId);
        List<Email> emails = emailRepository.findAllByUserId(userId);
        Account account = accountRepository.findByUserId(userId);
        return UserMapper.toDto(user, mobiles, emails, account.getAmount());
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User ID: " + userId + " not found"));
        List<Mobile> mobiles = mobileRepository.findAllByUserId(userId);
        List<Email> emails = emailRepository.findAllByUserId(userId);
        double userAmout = accountRepository.findByUserId(userId).getAmount();
        return UserMapper.toDto(user, mobiles, emails, userAmout);
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User ID: " + userId + " not found");
        }
        userRepository.deleteById(userId);

    }

    @Override
    public List<UserDto> getUsers(String name, LocalDate birthday, String number, String email, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users = userRepository.getUsers(name, birthday, number, email, pageable);

        return users.stream()
                .map(user -> UserMapper.toDto(user, mobileRepository.findAllByUserId(user.getId()),
                        emailRepository.findAllByUserId(user.getId()), accountRepository.findByUserId(user.getId()).getAmount()))
                .collect(Collectors.toList());
    }


    @Override
    public UserDto sendAmount(Long userId, Payment payment) {
        Long recipientId = payment.getRecipientId();
        if (!userRepository.existsById(recipientId)) {
            throw new NotFoundException("User ID: " + recipientId + " not found");
        }
        Account account = accountRepository.findByUserId(userId);
        double balance = account.getAmount();
        if (balance < payment.getPaymentAmount()) {
            throw new DataConflictException("Not sufficient funds");
        }
        double endBalance = balance - payment.getPaymentAmount();
        account.setAmount(endBalance);
        account.setUpdateAmountTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        accountRepository.save(account);

        //save Recipient's amount
        Account accountRecipient = accountRepository.findByUserId(recipientId);
        double balanceRecipient = accountRecipient.getAmount();
        double endBalanceRecipient = balanceRecipient + payment.getPaymentAmount();
        accountRecipient.setAmount(endBalanceRecipient);
        accountRecipient.setUpdateAmountTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        accountRepository.save(accountRecipient);

        User user = userRepository.findById(userId).get();
        List<Mobile> mobiles = mobileRepository.findAllByUserId(userId);
        List<Email> emails = emailRepository.findAllByUserId(userId);
        return UserMapper.toDto(user, mobiles, emails, endBalance);
    }


    //запуск обновления баланса (интервал 1 мин)
    public void updateBalance(Long userId) {
        Account account = accountRepository.findByUserId(userId);
        Runnable task = () -> {
            calcInterest(account);
        };
        scheduler.scheduleAtFixedRate(task, 1, 1, TimeUnit.MINUTES);
    }


    //расчет процентов
    public void calcInterest(Account account) {
        Long userId = account.getUserId();
        LocalDateTime accountUpdateAmountTime = account.getUpdateAmountTime();
        LocalDateTime currentUpdateAmountTime;
        double percent = 5.00;
        double maxPercent = 207.00;
        double accountAmount = account.getAmount();
        double maxAmountWithInterest = accountAmount * (1 + maxPercent / 100);
        double currentAmount;
        double currentAmountPlusInterest;
        Account savedAccount = accountRepository.findByUserId(userId);
        currentAmount = savedAccount.getAmount();
        currentUpdateAmountTime = savedAccount.getUpdateAmountTime();
        if (!currentUpdateAmountTime.equals(accountUpdateAmountTime)) {
            maxAmountWithInterest = currentAmount * (1 + maxPercent / 100);
            account.setAmount(currentAmount);
            account.setUpdateAmountTime(currentUpdateAmountTime);
            accountUpdateAmountTime = currentUpdateAmountTime;
        }
        currentAmountPlusInterest = currentAmount * (1 + percent / 100);
        if (currentAmountPlusInterest > maxAmountWithInterest) {
            currentAmountPlusInterest = currentAmount;
        }
        Account accountToSave = Account.builder()
                .id(account.getId())
                .userId(userId)
                .amount(currentAmountPlusInterest)
                .updateAmountTime(accountUpdateAmountTime)
                .build();
        accountRepository.save(accountToSave);
    }

}



