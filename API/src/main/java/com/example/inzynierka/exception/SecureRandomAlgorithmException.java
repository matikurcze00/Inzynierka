package com.example.inzynierka.exception;

public class SecureRandomAlgorithmException extends RuntimeException {

    public SecureRandomAlgorithmException(String message) {
        super(message);
    }

    public SecureRandomAlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SecureRandomAlgorithmException assigningSecureRandomException(String message) {
        return new SecureRandomAlgorithmException(message);
    }

}
