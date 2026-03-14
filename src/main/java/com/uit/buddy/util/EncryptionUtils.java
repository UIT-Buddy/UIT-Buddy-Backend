package com.uit.buddy.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EncryptionUtils {

  private final String algorithm;

  private static final int GCM_TAG_LENGTH = 128;
  private static final int GCM_IV_LENGTH = 12;

  private final SecretKey secretKey;
  private final SecureRandom secureRandom;

  public EncryptionUtils(
      @Value("${app.encryption.secret}") String secret,
      @Value("${app.encryption.algorithm}") String algorithm) {

    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length != 32) {
      log.error(
          "Critical: Encryption secret must be 32 bytes. Current length: {}", keyBytes.length);
      throw new IllegalArgumentException("Encryption secret must be exactly 32 bytes for AES-256");
    }

    this.algorithm = algorithm;
    this.secretKey = new SecretKeySpec(keyBytes, "AES");
    this.secureRandom = new SecureRandom();

    log.info("EncryptionUtils initialized with algorithm: {}", this.algorithm);
  }

  public String encrypt(String plaintext) {
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(algorithm);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

      ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
      byteBuffer.put(iv);
      byteBuffer.put(ciphertext);

      return Base64.getEncoder().encodeToString(byteBuffer.array());
    } catch (Exception e) {
      log.error("Encryption failed: {}", e.getMessage());
      throw new RuntimeException("Failed to encrypt data", e);
    }
  }

  public String decrypt(String encryptedData) {
    try {
      byte[] decoded = Base64.getDecoder().decode(encryptedData);

      ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
      byte[] iv = new byte[GCM_IV_LENGTH];
      byteBuffer.get(iv);
      byte[] ciphertext = new byte[byteBuffer.remaining()];
      byteBuffer.get(ciphertext);

      Cipher cipher = Cipher.getInstance(algorithm);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

      byte[] plaintext = cipher.doFinal(ciphertext);
      return new String(plaintext, StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.error("Decryption failed: {}", e.getMessage());
      throw new RuntimeException("Failed to decrypt data", e);
    }
  }
}
