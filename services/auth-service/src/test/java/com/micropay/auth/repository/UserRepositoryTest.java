package com.micropay.auth.repository;

import com.micropay.auth.model.User;
import com.micropay.auth.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByEmail returns saved user")
    void findByEmail() {
        User user = new User();
        user.setEmail("repo@example.com");
        user.setPassword("pw");
        user.setFirstName("Repo");
        user.setLastName("User");
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("repo@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("repo@example.com");
    }
}


