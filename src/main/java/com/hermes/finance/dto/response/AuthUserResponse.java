package com.hermes.finance.dto.response;

import java.util.UUID;

public record AuthUserResponse(
    UUID id,
    String email,
    String name,
    String role
) {
}
