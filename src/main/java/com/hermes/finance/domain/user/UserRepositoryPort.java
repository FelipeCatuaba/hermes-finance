package com.hermes.finance.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByExternalAuthId(String externalAuthId);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    void updateInternalCredentials(UUID id, String name, String passwordHash, String role, boolean active);

    void updateProfile(String externalAuthId, String email, String name);

    void anonymize(String externalAuthId);
}
