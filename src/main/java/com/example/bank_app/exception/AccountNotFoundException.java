package com.example.bank_app.exception;

public class AccountNotFoundException extends RuntimeException{

    private String message;

    public AccountNotFoundException(String accountId) {
        super("Account can not found by id: " + accountId);
    }
}
