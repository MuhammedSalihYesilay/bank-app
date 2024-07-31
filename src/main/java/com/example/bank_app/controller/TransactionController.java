package com.example.bank_app.controller;

import com.example.bank_app.dto.model.TransactionDto;
import com.example.bank_app.dto.request.NewMoneyTransferRequest;
import com.example.bank_app.entity.CustomerEntity;
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
public class TransactionController {

    private final TransactionService transactionService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("{accountId}/transfer-money")
    public TransactionDto addNewTransaction(@PathVariable String accountId, @RequestBody NewMoneyTransferRequest newMoneyTransferRequest, UriComponentsBuilder ucb, HttpServletResponse response) {
        CustomerEntity customerEntity = (CustomerEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TransactionDto transactionDto = transactionService.addNewTransaction(customerEntity.getId(), accountId, newMoneyTransferRequest);

        URI locationOfNewTransaction = ucb
                .path("{accountId}/transfer-money/{transactionDto}")
                .buildAndExpand(accountId, transactionDto.getId())
                .toUri();

        response.setHeader("Location", locationOfNewTransaction.toString());

        return transactionDto;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("{accountId}/transaction-history")
    public List<TransactionDto> getAllTransactions(@PathVariable String accountId) {
        CustomerEntity customerEntity = (CustomerEntity)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return transactionService.getAllTransactions(accountId, customerEntity.getId());

    }
}

