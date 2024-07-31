package com.example.bank_app.exception;

import java.math.BigDecimal;

public class BalanceIsNotEnoughException extends RuntimeException{

    public BalanceIsNotEnoughException(BigDecimal balance) {
        super("Balance is not enough for this transfer! Your balance: " + balance );
    }
}
