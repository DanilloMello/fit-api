package com.connecthealth.identity.infrastructure.persistence;

import com.connecthealth.identity.domain.entity.User;
import com.connecthealth.identity.domain.repository.UserRepository;
import com.connecthealth.identity.domain.valueobject.Email;
import com.connecthealth.identity.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserRepositoryImpl.class)
class UserRepositoryImplIntTest {

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
        User user = new User(UserId.generate(), "João Silva", new Email("joao@email.com"), "$2a$12$hash");

        userRepository.save(user);
        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("João Silva");
        assertThat(found.get().getEmail().getValue()).isEqualTo("joao@email.com");
    }

    @Test
    void save_and_find_by_email_returns_user() {
        User user = new User(UserId.generate(), "Maria Souza", new Email("maria@email.com"), "$2a$12$hash");

        userRepository.save(user);
        Optional<User> found = userRepository.findByEmail(new Email("maria@email.com"));

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Maria Souza");
    }

    @Test
    void exists_by_email_returns_true_after_save() {
        User user = new User(UserId.generate(), "Carlos Lima", new Email("carlos@email.com"), "$2a$12$hash");

        userRepository.save(user);

        assertThat(userRepository.existsByEmail(new Email("carlos@email.com"))).isTrue();
        assertThat(userRepository.existsByEmail(new Email("other@email.com"))).isFalse();
    }
}
