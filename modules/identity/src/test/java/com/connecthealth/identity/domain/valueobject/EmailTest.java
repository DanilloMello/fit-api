package com.connecthealth.identity.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void valid_email_is_accepted() {
        Email email = new Email("joao@email.com");
        assertThat(email.getValue()).isEqualTo("joao@email.com");
    }

    @Test
    void email_is_normalized_to_lowercase() {
        Email email = new Email("JOAO@Email.COM");
        assertThat(email.getValue()).isEqualTo("joao@email.com");
    }

    @Test
    void blank_email_throws_exception() {
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email must not be blank");
    }

    @Test
    void null_email_throws_exception() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalid_format_throws_exception_no_at_sign() {
        assertThatThrownBy(() -> new Email("notanemail"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    void invalid_format_throws_exception_starts_with_at() {
        assertThatThrownBy(() -> new Email("@domain.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    void emails_with_same_value_are_equal() {
        Email e1 = new Email("test@example.com");
        Email e2 = new Email("test@example.com");
        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }
}
