package com.example.javacryptoweb.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

@Service
public class FileEncryptionService  {
    private static final String ALGORITHM = "AES";

    public SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(128);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating encryption key", e);
        }
    }

    public byte[] encryptFile(byte[] fileBytes, SecretKey key) throws IllegalBlockSizeException, BadPaddingException, IOException {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(fileBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Error encrypting file", e);
        }
    }

    public byte[] decryptFile(byte[] encryptedBytes, SecretKey key) throws IllegalBlockSizeException, BadPaddingException, IOException {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Error decrypting file", e);
        }
    }
}
