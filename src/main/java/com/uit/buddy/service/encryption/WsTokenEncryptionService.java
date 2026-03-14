package com.uit.buddy.service.encryption;

public interface WsTokenEncryptionService {

    String encryptWstoken(String plainWstoken);

    String decryptWstoken(String encryptedWstoken);
}
