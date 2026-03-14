package com.uit.buddy.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncryptionUtilsTest {

    // AES-256 requires exactly 32 bytes
    private static final String VALID_SECRET = "12345678901234567890123456789012"; // 32 chars
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private EncryptionUtils encryptionUtils;

    @BeforeEach
    void setUp() {
        encryptionUtils = new EncryptionUtils(VALID_SECRET, ALGORITHM);
    }

    @Test
    void encrypt_plaintext_returnsNonNullCiphertext() {
        String ciphertext = encryptionUtils.encrypt("hello");
        assertNotNull(ciphertext);
        assertFalse(ciphertext.isBlank());
    }

    @Test
    void encrypt_thenDecrypt_returnsOriginalText() {
        String original = "test-payload-123";
        String encrypted = encryptionUtils.encrypt(original);
        String decrypted = encryptionUtils.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_sameInput_producesDifferentCiphertexts() {
        // Due to random IV each encrypt call should produce a different result
        String a = encryptionUtils.encrypt("same");
        String b = encryptionUtils.encrypt("same");
        assertNotEquals(a, b, "Each encryption should use a unique IV");
    }

    @Test
    void decrypt_bothCiphertexts_returnSamePlaintext() {
        String original = "same";
        String a = encryptionUtils.encrypt(original);
        String b = encryptionUtils.encrypt(original);
        assertEquals(encryptionUtils.decrypt(a), encryptionUtils.decrypt(b));
    }

    @Test
    void encrypt_emptyString_encryptsAndDecryptsSuccessfully() {
        String encrypted = encryptionUtils.encrypt("");
        String decrypted = encryptionUtils.decrypt(encrypted);
        assertEquals("", decrypted);
    }

    @Test
    void constructor_invalidSecretLength_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new EncryptionUtils("short", ALGORITHM));
    }

    @Test
    void decrypt_tamperedCiphertext_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> encryptionUtils.decrypt("this-is-not-valid-base64-ciphertext!!!!"));
    }
}
