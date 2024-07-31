package com.example.bank_app.exception;

public class AccountNotFoundByNumberException extends RuntimeException{

    public AccountNotFoundByNumberException(String accountNumber) {
        super("Account can not found by number: " + accountNumber);
    }
}
