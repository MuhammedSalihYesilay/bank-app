package com.example.bank_app.exception;

import java.math.BigDecimal;

public class AccountCreationException extends RuntimeException {

    public AccountCreationException() {
        super("Initial balance can not be negative!");
    }
}
