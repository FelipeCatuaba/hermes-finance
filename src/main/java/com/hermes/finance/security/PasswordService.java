package com.hermes.finance.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final Argon2PasswordEncoder encoder;

    public PasswordService(@Value("${security.password.argon2.memory-kb}") int memoryKb,
                           @Value("${security.password.argon2.iterations}") int iterations,
                           @Value("${security.password.argon2.parallelism}") int parallelism) {
        this.encoder = new Argon2PasswordEncoder(16, 32, parallelism, memoryKb, iterations);
    }

    public String hash(String rawPasswordWithPepper) {
        return encoder.encode(rawPasswordWithPepper);
    }

    public boolean verify(String rawPasswordWithPepper, String encodedPassword) {
        return encoder.matches(rawPasswordWithPepper, encodedPassword);
    }
}
