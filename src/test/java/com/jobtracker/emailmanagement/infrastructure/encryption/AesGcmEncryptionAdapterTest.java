package com.jobtracker.emailmanagement.infrastructure.encryption;

import com.jobtracker.emailmanagement.domain.exception.DecryptionFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesGcmEncryptionAdapterTest {

    private static final byte[] KEY_32 = new byte[32];
    static {
        new java.security.SecureRandom().nextBytes(KEY_32);
    }

    private AesGcmEncryptionAdapter adapter;

    @BeforeEach
    void setUp() {
        String keyBase64 = Base64.getEncoder().encodeToString(KEY_32);
        adapter = new AesGcmEncryptionAdapter(keyBase64, "");
        adapter.validateConfiguration();
    }

    @Test
    void encryptDecrypt_roundTrip() {
        String plainText = "Hello, World!";
        String cipherText = adapter.encrypt(plainText);
        String decrypted = adapter.decrypt(cipherText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void encryptDecrypt_roundTripWithSpecialCharacters() {
        String plainText = "{\"token\":\"ya29.a0AfH6SMB...\",\"expiry\":1617000000}";
        String cipherText = adapter.encrypt(plainText);
        String decrypted = adapter.decrypt(cipherText);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void decrypt_throwsOnInvalidCiphertext() {
        assertThatThrownBy(() -> adapter.decrypt("invalid-base64!!"))
                .isInstanceOf(DecryptionFailedException.class);
    }

    @Test
    void encrypt_isDeterministicallyDifferent() {
        String plainText = "Same text";
        String cipher1 = adapter.encrypt(plainText);
        String cipher2 = adapter.encrypt(plainText);
        assertThat(cipher1).isNotEqualTo(cipher2);
    }

    @Test
    void decrypt_throwsOnTamperedCiphertext() {
        String cipherText = adapter.encrypt("Sensitive data");
        byte[] tampered = Base64.getDecoder().decode(cipherText);
        tampered[tampered.length - 1] ^= 0x01;
        String tamperedBase64 = Base64.getEncoder().encodeToString(tampered);
        assertThatThrownBy(() -> adapter.decrypt(tamperedBase64))
                .isInstanceOf(DecryptionFailedException.class);
    }
}
