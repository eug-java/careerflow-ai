package com.careerflow.email.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialEncryptorTest {

    private final CredentialEncryptor encryptor = new CredentialEncryptor("0123456789abcdef0123456789abcdef");

    @Test
    void encryptsAndDecryptsPassword() {
        String encrypted = encryptor.encrypt("app-password-123");
        assertThat(encrypted).isNotBlank();
        assertThat(encryptor.decrypt(encrypted)).isEqualTo("app-password-123");
    }
}
