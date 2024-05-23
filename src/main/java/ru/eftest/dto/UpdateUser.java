package ru.eftest.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.eftest.model.State;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUser {
    @NotNull
    @Enumerated(EnumType.STRING)
    private State action;
    private String mobile;
    @Email
    private String email;
}
