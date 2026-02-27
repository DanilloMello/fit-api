package com.connecthealth.identity.security;

import com.connecthealth.identity.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        User user = new User("Alice", "alice@example.com", "hash123");
        principal = new UserPrincipal(user);
    }

    @Test
    void getUsername_returnsEmail() {
        assertEquals("alice@example.com", principal.getUsername());
    }

    @Test
    void getPassword_returnsPasswordHash() {
        assertEquals("hash123", principal.getPassword());
    }

    @Test
    void getAuthorities_containsRoleUser() {
        var authorities = principal.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void getId_returnsUserId() {
        // ID is null before persistence, but the method should not throw
        assertNull(principal.getId());
    }

    @Test
    void accountStatusMethods_returnTrue() {
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
        assertTrue(principal.isEnabled());
    }
}
