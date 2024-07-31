package com.example.bank_app.exception;

public class CustomerNotByFoundException extends RuntimeException {
    public CustomerNotByFoundException() {
        super("Customer not found in security context.");
    }
}
