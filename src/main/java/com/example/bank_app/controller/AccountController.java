package com.example.bank_app.controller;

import com.example.bank_app.dto.model.AccountDto;
import com.example.bank_app.dto.model.TransactionDto;
import com.example.bank_app.dto.request.NewAccountRequest;
import com.example.bank_app.dto.request.NewMoneyTransferRequest;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.entity.TransactionEntity;
import com.example.bank_app.service.AccountService;
import com.example.bank_app.service.TransactionService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto addNewAccount(@RequestBody NewAccountRequest newAccountRequest, UriComponentsBuilder ucb, HttpServletResponse response) {
        CustomerEntity customer = (CustomerEntity)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        AccountDto accountDto = accountService.addNewAccount(customer.getId(), newAccountRequest);

        URI locationOfNewAccount = ucb
                .path("/accounts/{id}")
                .buildAndExpand(accountDto.getId())
                .toUri();

        response.setHeader("Location", locationOfNewAccount.toString());

        return accountDto;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AccountDto> getAllAccounts() {
        CustomerEntity customerEntity = (CustomerEntity)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountService.getAllAccounts(customerEntity.getId());
    }

    @GetMapping("{accountId}")
    @ResponseStatus(HttpStatus.OK)
    public AccountDto getAccountByIdCustomerId(@PathVariable String accountId) {
        CustomerEntity customerEntity = (CustomerEntity)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        AccountDto accountDto = accountService.getAccountByIdAndCustomerId(accountId, customerEntity.getId());
        return accountDto;
    }

    @DeleteMapping("{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccountById(@PathVariable String accountId) {
        CustomerEntity customerEntity = (CustomerEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        accountService.deleteAccountById(accountId, customerEntity.getId());
    }
}
