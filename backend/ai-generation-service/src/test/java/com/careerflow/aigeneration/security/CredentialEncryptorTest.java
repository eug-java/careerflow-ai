package com.careerflow.aigeneration.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialEncryptorTest {

    @Test
    void encryptAndDecryptShouldRoundTrip() {
        CredentialEncryptor encryptor = new CredentialEncryptor("0123456789abcdef0123456789abcdef");

        String encrypted = encryptor.encrypt("sk-test-api-key");
        String decrypted = encryptor.decrypt(encrypted);

        assertThat(decrypted).isEqualTo("sk-test-api-key");
        assertThat(encrypted).isNotEqualTo("sk-test-api-key");
    }
}
