package com.connecthealth.identity.repository;

import com.connecthealth.identity.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("connecthealth_test")
            .withUsername("connecthealth")
            .withPassword("connecthealth");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_and_find_by_id_returns_user() {
        User user = new User(UUID.randomUUID(), "João Silva", "joao@email.com",
                "$2a$12$hash", null, null, Instant.now());

        userRepository.save(user);
        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("João Silva");
        assertThat(found.get().getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    void save_and_find_by_email_returns_user() {
        User user = new User(UUID.randomUUID(), "Maria Souza", "maria@email.com",
                "$2a$12$hash", null, null, Instant.now());

        userRepository.save(user);
        Optional<User> found = userRepository.findByEmail("maria@email.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Maria Souza");
    }

    @Test
    void exists_by_email_returns_true_after_save() {
        User user = new User(UUID.randomUUID(), "Carlos Lima", "carlos@email.com",
                "$2a$12$hash", null, null, Instant.now());

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("carlos@email.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@email.com")).isFalse();
    }
}
