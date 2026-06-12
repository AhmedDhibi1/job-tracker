package com.jobtracker.emailmanagement.infrastructure.encryption;

import com.jobtracker.emailmanagement.application.port.outbound.EmailEncryptionPort;
import com.jobtracker.emailmanagement.domain.exception.DecryptionFailedException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesGcmEncryptionAdapter implements EmailEncryptionPort {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final byte[] currentKey;
    private final byte[] previousKey;

    public AesGcmEncryptionAdapter(
            @Value("${ENCRYPTION_KEY}") String currentKeyBase64,
            @Value("${ENCRYPTION_KEY_PREVIOUS:}") String previousKeyBase64) {
        this.currentKey = Base64.getDecoder().decode(currentKeyBase64);
        if (previousKeyBase64 != null && !previousKeyBase64.isBlank()) {
            this.previousKey = Base64.getDecoder().decode(previousKeyBase64);
        } else {
            this.previousKey = null;
        }
    }

    @PostConstruct
    public void validateConfiguration() {
        if (currentKey == null || currentKey.length != 32) {
            throw new IllegalStateException(
                    "ENCRYPTION_KEY must be a 256-bit (32-byte) AES key. Got "
                    + (currentKey == null ? "null" : currentKey.length) + " bytes.");
        }
    }

    @Override
    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(currentKey, ALGORITHM), parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (AEADBadTagException e) {
            throw new EncryptionException("Encryption failed: authentication tag mismatch", e);
        } catch (IllegalBlockSizeException e) {
            throw new EncryptionException("Encryption failed: invalid block size", e);
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        try {
            return decryptWithKey(cipherText, currentKey);
        } catch (AEADBadTagException e) {
            if (previousKey != null) {
                try {
                    return decryptWithKey(cipherText, previousKey);
                } catch (AEADBadTagException ex) {
                    throw new DecryptionFailedException(
                            "Authentication failed with both current and previous keys: possible tampering", ex);
                } catch (Exception ex) {
                    throw new DecryptionFailedException("Decryption failed with previous key", ex);
                }
            }
            throw new DecryptionFailedException("Authentication failed with current key: possible tampering", e);
        } catch (IllegalArgumentException e) {
            throw new DecryptionFailedException("Invalid ciphertext format", e);
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }

    private String decryptWithKey(String cipherTextBase64, byte[] key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(cipherTextBase64);

        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM), parameterSpec);

        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }

    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
