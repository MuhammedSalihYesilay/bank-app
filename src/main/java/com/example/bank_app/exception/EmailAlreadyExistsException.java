package com.example.bank_app.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email " + email + " already exists!");
    }
}
