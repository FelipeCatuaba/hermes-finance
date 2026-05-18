package com.hermes.finance.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        User user = new User();
        user.setName("Felipe");
        user.setEmail("felipe@email.com");
        user.setPassword("hashed-password");
        user.setRole("OWNER");
        user.setActive(true);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("felipe@email.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("felipe@email.com");
    }

    @Test
    void shouldFindUserById() {
        User user = new User();
        user.setName("Ana");
        user.setEmail("ana@email.com");
        user.setPassword("hashed-password");
        user.setRole("OWNER");
        user.setActive(true);
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldUpdatePassword() {
        User user = new User();
        user.setName("Mario");
        user.setEmail("mario@email.com");
        user.setPassword("old-password");
        user.setRole("OWNER");
        user.setActive(true);
        User saved = userRepository.save(user);

        userRepository.updatePassword(saved.getId(), "new-password");
        Optional<User> updated = userRepository.findById(saved.getId());

        assertThat(updated).isPresent();
        assertThat(updated.get().getPassword()).isEqualTo("new-password");
    }
}
