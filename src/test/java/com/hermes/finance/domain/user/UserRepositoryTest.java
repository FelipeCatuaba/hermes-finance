package com.hermes.finance.domain.user;

import com.hermes.finance.config.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
}
