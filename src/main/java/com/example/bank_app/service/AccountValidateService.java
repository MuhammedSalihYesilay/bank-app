package com.example.bank_app.service;

import com.example.bank_app.entity.AccountEntity;
import com.example.bank_app.exception.AccountNotFoundException;
import com.example.bank_app.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountValidateService {

    private final AccountRepository accountRepository;

    public AccountEntity retrieveAndValidateAccountByIdAndCustomerId(String accountId, String customerId) {
        return accountRepository.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
