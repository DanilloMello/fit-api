package com.connecthealth.identity.domain.entity;

import com.connecthealth.identity.domain.valueobject.Email;
import com.connecthealth.identity.domain.valueobject.UserId;
import com.connecthealth.shared.domain.AggregateRoot;

import java.time.Instant;

public class User extends AggregateRoot<UserId> {

    private final String name;
    private final Email email;
    private final String passwordHash;
    private final String phone;
    private final String photoUrl;
    private final Instant createdAt;

    public User(UserId id, String name, Email email, String passwordHash, String phone, String photoUrl, Instant createdAt) {
        super(id);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public User(UserId id, String name, Email email, String passwordHash) {
        this(id, name, email, passwordHash, null, null, Instant.now());
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
