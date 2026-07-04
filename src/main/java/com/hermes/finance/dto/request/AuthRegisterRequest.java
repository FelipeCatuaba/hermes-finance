package com.hermes.finance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
    @NotBlank @Size(min = 2, max = 255) String name,
    @Email @NotBlank String email,
    @NotBlank
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Senha deve ter 8+ caracteres, maiuscula, minuscula, numero e simbolo"
    )
    String password
) {
}
