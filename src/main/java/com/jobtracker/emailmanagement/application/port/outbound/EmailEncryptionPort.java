package com.jobtracker.emailmanagement.application.port.outbound;


public interface EmailEncryptionPort {

    String encrypt(String plainText);

    String decrypt(String cipherText);
}