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

    @Test
    void decrypt_withPreviousKey_afterCurrentKeyFails() {
        byte[] previousKeyBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(previousKeyBytes);
        String previousKeyBase64 = Base64.getEncoder().encodeToString(previousKeyBytes);

        AesGcmEncryptionAdapter oldAdapter = new AesGcmEncryptionAdapter(previousKeyBase64, "");
        String cipherText = oldAdapter.encrypt("Data encrypted with previous key");

        AesGcmEncryptionAdapter newAdapter = new AesGcmEncryptionAdapter(
                Base64.getEncoder().encodeToString(KEY_32), previousKeyBase64);
        newAdapter.validateConfiguration();

        String decrypted = newAdapter.decrypt(cipherText);
        assertThat(decrypted).isEqualTo("Data encrypted with previous key");
    }

    @Test
    void decrypt_throws_whenBothKeysFail() {
        byte[] key1 = new byte[32];
        byte[] key2 = new byte[32];
        byte[] key3 = new byte[32];
        new java.security.SecureRandom().nextBytes(key1);
        new java.security.SecureRandom().nextBytes(key2);
        new java.security.SecureRandom().nextBytes(key3);

        AesGcmEncryptionAdapter adapter1 = new AesGcmEncryptionAdapter(
                Base64.getEncoder().encodeToString(key1), "");
        String cipherText = adapter1.encrypt("secret");

        AesGcmEncryptionAdapter adapter2 = new AesGcmEncryptionAdapter(
                Base64.getEncoder().encodeToString(key2),
                Base64.getEncoder().encodeToString(key3));
        adapter2.validateConfiguration();

        assertThatThrownBy(() -> adapter2.decrypt(cipherText))
                .isInstanceOf(DecryptionFailedException.class)
                .hasMessageContaining("Authentication failed");
    }

    @Test
    void decrypt_withPreviousKey_whenCurrentFailsDueToBadTag() {
        byte[] key1 = new byte[32];
        byte[] key2 = new byte[32];
        new java.security.SecureRandom().nextBytes(key1);
        new java.security.SecureRandom().nextBytes(key2);

        AesGcmEncryptionAdapter adapter1 = new AesGcmEncryptionAdapter(
                Base64.getEncoder().encodeToString(key1), "");
        String cipherText = adapter1.encrypt("secret");

        AesGcmEncryptionAdapter adapter2 = new AesGcmEncryptionAdapter(
                Base64.getEncoder().encodeToString(key2),
                Base64.getEncoder().encodeToString(key1));
        adapter2.validateConfiguration();

        String decrypted = adapter2.decrypt(cipherText);
        assertThat(decrypted).isEqualTo("secret");
    }

    @Test
    void validateConfiguration_rejectsInvalidKeyLength() {
        byte[] shortKey = new byte[16];
        new java.security.SecureRandom().nextBytes(shortKey);
        String shortKeyBase64 = Base64.getEncoder().encodeToString(shortKey);
        AesGcmEncryptionAdapter badAdapter = new AesGcmEncryptionAdapter(shortKeyBase64, "");
        assertThatThrownBy(badAdapter::validateConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256-bit");
    }

    @Test
    void encrypt_handlesEmptyString() {
        String cipherText = adapter.encrypt("");
        String decrypted = adapter.decrypt(cipherText);
        assertThat(decrypted).isEmpty();
    }

    @Test
    void decryptionFailedException_isThrownOnBadBase64() {
        assertThatThrownBy(() -> adapter.decrypt("!@#$%^"))
                .isInstanceOf(DecryptionFailedException.class);
    }

    @Test
    void encrypt_throwsEncryptionException_onNullInput() {
        assertThatThrownBy(() -> adapter.encrypt(null))
                .isInstanceOf(AesGcmEncryptionAdapter.EncryptionException.class)
                .hasMessageContaining("Encryption failed");
    }

    @Test
    void decrypt_throwsEncryptionException_onNullInput() {
        assertThatThrownBy(() -> adapter.decrypt(null))
                .isInstanceOf(AesGcmEncryptionAdapter.EncryptionException.class)
                .hasMessageContaining("Decryption failed");
    }
}
