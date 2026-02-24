package com.uit.buddy.service.encryption.impl;

import com.uit.buddy.service.encryption.WsTokenEncryptionService;
import com.uit.buddy.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WsTokenEncryptionServiceImpl implements WsTokenEncryptionService {

    private final EncryptionUtils encryptionUtils;

    @Override
    public String encryptWstoken(String plainWstoken) {
        if (plainWstoken == null || plainWstoken.isBlank()) {
            throw new IllegalArgumentException("Wstoken cannot be null or empty");
        }

        log.debug("Encrypting wstoken");
        return encryptionUtils.encrypt(plainWstoken);
    }

    @Override
    public String decryptWstoken(String encryptedWstoken) {
        if (encryptedWstoken == null || encryptedWstoken.isBlank()) {
            throw new IllegalArgumentException("Encrypted wstoken cannot be null or empty");
        }

        log.debug("Decrypting wstoken");
        return encryptionUtils.decrypt(encryptedWstoken);
    }
}
