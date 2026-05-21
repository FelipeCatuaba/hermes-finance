package com.hermes.finance.domain.user;

import com.hermes.finance.config.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestJwtDecoderConfig.class)
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByExternalAuthId() {
        User user = new User();
        user.setExternalAuthId("user_test_external_001");
        user.setName("Felipe");
        user.setEmail("felipe@email.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByExternalAuthId("user_test_external_001");

        assertThat(found).isPresent();
        assertThat(found.get().getExternalAuthId()).isEqualTo("user_test_external_001");
        assertThat(found.get().getEmail()).isEqualTo("felipe@email.com");
    }

    @Test
    void shouldFindUserById() {
        User user = new User();
        user.setExternalAuthId("user_test_external_002");
        user.setName("Ana");
        user.setEmail("ana@email.com");
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldAnonymizeUser() {
        User user = new User();
        user.setExternalAuthId("user_test_external_003");
        user.setName("Mario");
        user.setEmail("mario@email.com");
        userRepository.save(user);

        userRepository.anonymize("user_test_external_003");

        Optional<User> updated = userRepository.findByExternalAuthId("user_test_external_003");
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Usuário Removido");
        assertThat(updated.get().getEmail()).contains("@deleted.invalid");
    }

    @Test
    void shouldPreserveProvidedIdAndCreatedAtOnSave() {
        UUID providedId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(2);

        User user = new User();
        user.setId(providedId);
        user.setCreatedAt(createdAt);
        user.setExternalAuthId("user_test_external_004");
        user.setName("Joao");
        user.setEmail("joao@email.com");

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isEqualTo(providedId);
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateUserProfile() {
        User user = new User();
        user.setExternalAuthId("user_test_external_005");
        user.setName("Old Name");
        user.setEmail("old@email.com");
        userRepository.save(user);

        userRepository.updateProfile("user_test_external_005", "new@email.com", "New Name");

        Optional<User> updated = userRepository.findByExternalAuthId("user_test_external_005");
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("New Name");
        assertThat(updated.get().getEmail()).isEqualTo("new@email.com");
    }

    @Test
    void shouldReturnEmptyWhenExternalAuthIdDoesNotExist() {
        Optional<User> missing = userRepository.findByExternalAuthId("missing-user");
        assertThat(missing).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        Optional<User> missing = userRepository.findById(UUID.randomUUID());
        assertThat(missing).isEmpty();
    }
}
