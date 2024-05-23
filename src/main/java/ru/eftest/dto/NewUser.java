package ru.eftest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewUser {
    @NotBlank
    private String name;
    @NotNull
    private LocalDate birthday;
    @NotBlank
    private String login;
    @NotBlank
    private String password;
    @NotBlank
    private String mobile;
    @Email
    @NotNull
    private String email;
    @NotNull
    private double amount;
}
