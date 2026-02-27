package com.connecthealth.identity.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void constructor_setsFields() {
        User user = new User("Alice", "alice@example.com", "hash123");

        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("hash123", user.getPasswordHash());
        assertNull(user.getId());
        assertNull(user.getPhone());
        assertNull(user.getPhotoUrl());
        assertNull(user.getRefreshToken());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void setters_updateFields() {
        User user = new User("Alice", "alice@example.com", "hash123");

        user.setName("Bob");
        user.setPhone("+5511999990000");
        user.setPhotoUrl("https://example.com/photo.jpg");
        user.setRefreshToken("some-refresh-token");

        assertEquals("Bob", user.getName());
        assertEquals("+5511999990000", user.getPhone());
        assertEquals("https://example.com/photo.jpg", user.getPhotoUrl());
        assertEquals("some-refresh-token", user.getRefreshToken());
    }
}
