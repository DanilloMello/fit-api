package com.connecthealth.identity.application.usecase;

public record RegisterUserCommand(String name, String email, String password) {
}
