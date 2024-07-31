package com.example.bank_app.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super("Customer not found for id : " + customerId);
    }
}

