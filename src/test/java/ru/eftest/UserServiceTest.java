package ru.eftest;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import ru.eftest.dto.UserDto;
import ru.eftest.mapper.UserMapper;
import ru.eftest.model.*;
import ru.eftest.repository.AccountRepository;
import ru.eftest.repository.EmailRepository;
import ru.eftest.repository.MobileRepository;
import ru.eftest.repository.UserRepository;
import ru.eftest.service.UserServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceTest {

    @InjectMocks
    UserServiceImpl userService;
    @Mock
    UserRepository userRepository;
    @Mock
    AccountRepository accountRepository;
    @Mock
    MobileRepository mobileRepository;
    @Mock
    EmailRepository emailRepository;

    User user;
    Account accountSender;
    Account accountRecipient;

    @BeforeEach
    void setUp() {
        user = createUser();
        accountSender = createAccountSender();
        accountRecipient = createAccountRecipient();
    }

    @Test
    void sendAmount() {
        Payment payment = Payment.builder()
                .recipientId(2L)
                .paymentAmount(25.00)
                .build();

        doReturn(true)
                .when(userRepository).existsById(anyLong());
        when(accountRepository.findByUserId(1L))
                .thenReturn(accountSender);
        when(accountRepository.save(any()))
                .thenReturn(createAccountSender());
        when(accountRepository.findByUserId(2L))
                .thenReturn(accountRecipient);
        when(accountRepository.save(any()))
                .thenReturn(createAccountRecipient());
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(mobileRepository.findAllByUserId(anyLong()))
                .thenReturn(createMobiles());
        when(emailRepository.findAllByUserId(anyLong()))
                .thenReturn(createEmails());

        UserDto senderBalance = userService.sendAmount(1L, payment);
        assertThat(senderBalance.getAmount(), equalTo(75.0));
    }


    private User createUser() {
        User user = User.builder()
                .id(1L)
                .name("TestSenderName")
                .birthday(LocalDate.of(2000, 01, 01))
                .login("testSenderLogin")
                .password("testSenderPassword")
                .build();
        return user;
    }

    private UserDto createSender() {
        User sender = User.builder()
                .id(1L)
                .name("TestSenderName")
                .birthday(LocalDate.of(2000, 01, 01))
                .login("testSenderLogin")
                .password("testSenderPassword")
                .build();
        List<Mobile> mobileSenderList = new ArrayList<>();
        List<Email> emailSenderList = new ArrayList<>();
        double amount = 100.00;
        Mobile mobileSender = Mobile.builder()
                .userId(1L)
                .number("1111111")
                .build();
        mobileSenderList.add(mobileSender);
        Email emailSender = Email.builder()
                .userId(1L)
                .email("userSender@mail.com")
                .build();
        emailSenderList.add(emailSender);
        return UserMapper.toDto(sender, mobileSenderList, emailSenderList, amount);
    }

    private List<Mobile> createMobiles() {
        List<Mobile> mobileSenderList = new ArrayList<>();
        Mobile mobileSender = Mobile.builder()
                .userId(1L)
                .number("1111111")
                .build();
        mobileSenderList.add(mobileSender);
        return mobileSenderList;

    }

    private List<Email> createEmails() {
        List<Email> emailSenderList = new ArrayList<>();
        Email emailSender = Email.builder()
                .userId(1L)
                .email("userSender@mail.com")
                .build();
        emailSenderList.add(emailSender);
        return emailSenderList;
    }

    private UserDto createRecipient() {
        User recipient = User.builder()
                .id(2L)
                .name("TestRecipientName")
                .birthday(LocalDate.of(2002, 02, 02))
                .login("testRecipientLogin")
                .password("testRecipientPassword")
                .build();
        double amount = 1000.00;
        List<Mobile> mobileRecipientList = new ArrayList<>();
        List<Email> emailRecipientList = new ArrayList<>();
        Mobile mobileRecipient = Mobile.builder()
                .userId(2L)
                .number("2222222")
                .build();
        mobileRecipientList.add(mobileRecipient);
        Email emailRecipient = Email.builder()
                .userId(2L)
                .email("userRecipient@mail.com")
                .build();
        emailRecipientList.add(emailRecipient);
        return UserMapper.toDto(recipient, mobileRecipientList, emailRecipientList, amount);
    }

    private Account createAccountSender() {
        Account account = Account.builder()
                .id(1L)
                .userId(1L)
                .amount(100.00)
                .updateAmountTime(LocalDateTime.now())
                .build();
        return account;
    }

    private Account createAccountRecipient() {
        Account account = Account.builder()
                .id(2L)
                .userId(2L)
                .amount(1000.00)
                .updateAmountTime(LocalDateTime.now())
                .build();
        return account;
    }

}
