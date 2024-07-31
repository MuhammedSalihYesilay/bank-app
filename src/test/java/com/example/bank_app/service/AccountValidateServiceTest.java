package com.example.bank_app.service;

import com.example.bank_app.entity.AccountEntity;
import com.example.bank_app.exception.AccountNotFoundException;
import com.example.bank_app.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class AccountValidateServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountValidateService accountValidateService;

    @Test
    void shouldRetrieveAccount_whenAccountFoundByAccountIdAndCustomerId() {
        String accountId = "accountId";
        String customerId = "customerId";

        AccountEntity accountEntity = AccountEntity
                .builder()
                .id(accountId)
                .customerId(customerId)
                .accountNumber("accountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(accountRepository.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(accountEntity));

        AccountEntity result = accountValidateService.retrieveAndValidateAccountByIdAndCustomerId(accountId, customerId);

        assertThat(result).isNotNull();

        Mockito.verify(accountRepository).findByIdAndCustomerId(accountId, customerId);
    }

    @Test
    void shouldNotRetrieveAccount_whenAccountNotFoundByAccountIdAndCustomerId() {
        String accountId = "accountId";
        String customerId = "customerId";

        Mockito.when(accountRepository.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountValidateService.retrieveAndValidateAccountByIdAndCustomerId(accountId, customerId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account can not found by id: " + accountId);
    }
}
