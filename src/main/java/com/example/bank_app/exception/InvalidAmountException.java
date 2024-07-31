package com.example.bank_app.exception;

public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException() {
        super("Invalid amount! Please enter a value greater than 0.");
    }
}
