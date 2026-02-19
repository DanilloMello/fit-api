package com.connecthealth.identity.domain.entity;

import com.connecthealth.identity.domain.valueobject.Email;
import com.connecthealth.identity.domain.valueobject.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final UserId VALID_ID = UserId.generate();
    private static final Email VALID_EMAIL = new Email("user@example.com");
    private static final String VALID_PASSWORD_HASH = "$2a$12$hash";

    @Test
    void user_created_with_valid_fields() {
        User user = new User(VALID_ID, "João Silva", VALID_EMAIL, VALID_PASSWORD_HASH);

        assertThat(user.getId()).isEqualTo(VALID_ID);
        assertThat(user.getName()).isEqualTo("João Silva");
        assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(user.getPasswordHash()).isEqualTo(VALID_PASSWORD_HASH);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void user_creation_fails_with_blank_name() {
        assertThatThrownBy(() -> new User(VALID_ID, "   ", VALID_EMAIL, VALID_PASSWORD_HASH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must not be blank");
    }

    @Test
    void user_creation_fails_with_null_name() {
        assertThatThrownBy(() -> new User(VALID_ID, null, VALID_EMAIL, VALID_PASSWORD_HASH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must not be blank");
    }

    @Test
    void user_creation_fails_with_null_email() {
        assertThatThrownBy(() -> new User(VALID_ID, "João", null, VALID_PASSWORD_HASH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must not be null");
    }

    @Test
    void user_creation_fails_with_blank_password_hash() {
        assertThatThrownBy(() -> new User(VALID_ID, "João", VALID_EMAIL, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password hash must not be blank");
    }

    @Test
    void user_optional_fields_are_nullable() {
        User user = new User(VALID_ID, "João", VALID_EMAIL, VALID_PASSWORD_HASH);
        assertThat(user.getPhone()).isNull();
        assertThat(user.getPhotoUrl()).isNull();
    }
}
