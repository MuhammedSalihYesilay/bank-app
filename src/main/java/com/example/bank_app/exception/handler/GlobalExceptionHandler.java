package com.example.bank_app.exception.handler;


import com.example.bank_app.dto.response.ErrorResponse;
import com.example.bank_app.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EmailAlreadyExistsException.class)
    protected ErrorResponse handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CustomerNotFoundException.class)
    protected ErrorResponse handleCustomerNotFoundException(CustomerNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AccountNotFoundException.class)
    protected ErrorResponse handleAccountNotFoundException(AccountNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AccountCreationException.class)
    protected ErrorResponse handleAccountCreationException(AccountCreationException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BalanceIsNotEnoughException.class)
    protected ErrorResponse handleBalanceIsNotEnoughException(BalanceIsNotEnoughException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidAmountException.class)
    protected ErrorResponse handleInvalidAmountException(InvalidAmountException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AccountNotFoundByNumberException.class)
    protected ErrorResponse handleAccountNotFoundByNumberException(AccountNotFoundByNumberException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}


