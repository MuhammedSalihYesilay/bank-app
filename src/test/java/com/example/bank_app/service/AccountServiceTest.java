package com.example.bank_app.service;

import com.example.bank_app.common.TransactionType;
import com.example.bank_app.dto.model.AccountDto;
import com.example.bank_app.dto.request.NewAccountRequest;
import com.example.bank_app.entity.AccountEntity;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.entity.TransactionEntity;
import com.example.bank_app.exception.AccountCreationException;
import com.example.bank_app.exception.AccountNotFoundException;
import com.example.bank_app.repository.AccountRepository;
import com.example.bank_app.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountValidateService accountValidateService;

    @Captor
    private ArgumentCaptor<AccountEntity> accountEntityCaptor;

    @Captor
    private ArgumentCaptor<TransactionEntity> transactionCaptor;

    @InjectMocks
    private AccountService accountService;


    @Test
    void whenAddNewAccountCalledWithZeroInitialBalance_shouldCreateAccountDto() {
        String customerId = "customerId";

        NewAccountRequest request = new NewAccountRequest();
        request.setInitialBalance(BigDecimal.valueOf(0));

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setAccountNumber("generatedAccountNumber");
        accountEntity.setBalance(request.getInitialBalance());
        accountEntity.setCustomerId(customerId);

        AccountDto accountDto = AccountDto.builder()
                .id("account-id")
                .customerId(customerId)
                .accountNumber(accountEntity.getAccountNumber())
                .balance(accountEntity.getBalance())
                .createdAt(LocalDateTime.now())
                .build();


        when(accountRepository.save(accountEntityCaptor.capture())).thenReturn(accountEntity);
        when(modelMapper.map(accountEntity, AccountDto.class)).thenReturn(accountDto);

        AccountDto result = accountService.addNewAccount(customerId, request);

        assertEquals(result, accountDto);
        verify(accountRepository, times(1)).save(Mockito.any(AccountEntity.class));

        AccountEntity capturedAccount = accountEntityCaptor.getValue();

        assertEquals(customerId, capturedAccount.getCustomerId());
        assertEquals(request.getInitialBalance(), capturedAccount.getBalance());

        verify(modelMapper, times(1)).map(accountEntity, AccountDto.class);
    }

    @Test
    void whenAddNewAccountCalledWithNegativeInitialBalance_shouldThrowAccountCreationException() {
        String customerId = "customerId";

        NewAccountRequest request = new NewAccountRequest();
        request.setInitialBalance(BigDecimal.valueOf(-1000));

        assertThrows(AccountCreationException.class, () -> accountService.addNewAccount(customerId, request));
    }

    @Test
    void whenCreateInitialTransactionIfNecessaryCalledWithPositiveInitialBalance_shouldCreateTransaction() {
        String customerId = "customerId";

        NewAccountRequest request = new NewAccountRequest();
        request.setInitialBalance(BigDecimal.valueOf(1000));

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId("accountId");
        accountEntity.setAccountNumber("generatedAccountNumber");
        accountEntity.setBalance(request.getInitialBalance());
        accountEntity.setCustomerId(customerId);

        AccountDto accountDto = AccountDto.builder()
                .id("accountId")
                .customerId(customerId)
                .accountNumber(accountEntity.getAccountNumber())
                .balance(accountEntity.getBalance())
                .createdAt(LocalDateTime.now())
                .build();


        when(accountRepository.save(accountEntityCaptor.capture())).thenReturn(accountEntity);
        when(transactionRepository.save(transactionCaptor.capture())).thenReturn(null);
        when(modelMapper.map(accountEntity, AccountDto.class)).thenReturn(accountDto);

        AccountDto result = accountService.addNewAccount(customerId, request);

        assertEquals(result, accountDto);

        verify(accountRepository, times(1)).save(Mockito.any(AccountEntity.class));
        AccountEntity capturedAccount = accountEntityCaptor.getValue();
        assertEquals(customerId, capturedAccount.getCustomerId());
        assertEquals(request.getInitialBalance(), capturedAccount.getBalance());

        verify(transactionRepository, times(1)).save(Mockito.any(TransactionEntity.class));
        TransactionEntity capturedTransaction = transactionCaptor.getValue();
        assertEquals(capturedTransaction.getTransactionType(), TransactionType.INITIAL);
        assertEquals(capturedTransaction.getSenderAccountId(), accountEntity.getId());
        assertEquals(capturedTransaction.getReceiverAccountId(), accountEntity.getId());
        assertEquals(capturedTransaction.getAmount(), request.getInitialBalance());

        verify(modelMapper, times(1)).map(accountEntity, AccountDto.class);
    }

    @Test
    void whenGetAllAccountsCalledWithExistingAccounts_shouldReturnListOfAccounts() {
        String customerId = "customerId";

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setAccountNumber("1234567890123456");
        accountEntity.setBalance(BigDecimal.valueOf(1000));
        accountEntity.setCustomerId(customerId);

        AccountDto accountDto = AccountDto.builder()
                .id("account-id")
                .customerId(customerId)
                .accountNumber(accountEntity.getAccountNumber())
                .balance(accountEntity.getBalance())
                .createdAt(LocalDateTime.now())
                .build();

        List<AccountDto> expected = List.of(accountDto);

        when(accountRepository.findAllByCustomerId(customerId)).thenReturn(List.of(accountEntity));
        when(modelMapper.map(accountEntity, AccountDto.class)).thenReturn(accountDto);

        List<AccountDto> result = accountService.getAllAccounts(customerId);

        assertEquals(expected, result);
        assertEquals(1, result.size());

        verify(accountRepository, times(1)).findAllByCustomerId(customerId);
        verify(modelMapper, times(1)).map(accountEntity, AccountDto.class);
    }


    @Test
    @DisplayName("Should return the requested account when the account found by account id and customer id.")
    public void shouldReturnTheRequestedAccount_whenTheAccountFountByAccountIdAndCustomerId() {
        AccountEntity accountEntity = AccountEntity
                .builder()
                .id("accountId")
                .customerId("customerId")
                .accountNumber("accountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        AccountDto expectedResult = AccountDto
                .builder()
                .id("accountId")
                .accountNumber("accountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(accountValidateService.retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId")).thenReturn(accountEntity);
        Mockito.when(modelMapper.map(accountEntity, AccountDto.class)).thenReturn(expectedResult);

        AccountDto result = accountService.getAccountByIdAndCustomerId("accountId", "customerId");

        assertEquals(expectedResult, result);

        Mockito.verify(accountValidateService).retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId");
        Mockito.verify(modelMapper).map(accountEntity, AccountDto.class);
    }

    @Test
    @DisplayName("Should not return the account when the account not found by account id and customer id.")
    public void shouldNotReturnTheRequestedAccount_whenTheAccountNotFountByAccountIdAndCustomerId() {
        Mockito.when(accountValidateService.retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId"))
                .thenThrow(new AccountNotFoundException("accountId"));

        assertThatThrownBy(() -> accountService.getAccountByIdAndCustomerId("accountId", "customerId"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account can not found by id: " + "accountId");

        Mockito.verify(accountValidateService).retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId");
        Mockito.verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("Should delete the requested account and should delete the initial transaction when the account found by account id and customer id and there was an initial transaction.")
    public void shouldDeleteTheRequestedAccountAndShouldDeleteTheInitialTransaction_whenTheAccountFoundByAccountIdAndCustomerIdAndThereWasAnInitialTransaction() {
        AccountEntity accountEntity = AccountEntity
                .builder()
                .id("accountId")
                .customerId("customerId")
                .accountNumber("accountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        TransactionEntity transactionEntity = TransactionEntity
                .builder()
                .id("transactionId")
                .senderAccountId("senderAccountId")
                .receiverAccountId("receiverAccountId")
                .transactionType(TransactionType.INITIAL)
                .amount(BigDecimal.TEN)
                .date(LocalDateTime.now())
                .description("description.")
                .build();

        Mockito.when(accountValidateService.retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId")).thenReturn((accountEntity));
        Mockito.when(transactionRepository.findInitialTransactionByAccountId("accountId")).thenReturn(Optional.of(transactionEntity));

        accountService.deleteAccountById("accountId", "customerId");

        Mockito.verify(accountValidateService).retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId");
        Mockito.verify(transactionRepository).findInitialTransactionByAccountId("accountId");
        Mockito.verify(transactionRepository).delete(transactionEntity);
        Mockito.verify(accountRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Should delete the requested account but should not delete any initial transaction when the account found by account id and customer id and there was no an initial transaction.")
    public void shouldDeleteTheRequestedAccountButShouldNotDeleteAnyInitialTransaction_whenTheAccountFoundByAccountIdAndCustomerIdsAndThereWasNoAnInitialTransaction() {
        AccountEntity accountEntity = AccountEntity
                .builder()
                .id("accountId")
                .customerId("customerId")
                .accountNumber("accountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(accountValidateService.retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId")).thenReturn((accountEntity));
        Mockito.when(transactionRepository.findInitialTransactionByAccountId("accountId")).thenReturn(Optional.empty());

        accountService.deleteAccountById("accountId", "customerId");

        Mockito.verify(accountValidateService).retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId");
        Mockito.verify(transactionRepository).findInitialTransactionByAccountId("accountId");
        Mockito.verify(accountRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Should not delete any account when the account not found by account id  and customer id")
    void shouldNotDeleteAnyAccount_whenTheAccountNotFoundByAccountIdAndCustomerId() {
        Mockito.when(accountValidateService.retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId"))
                .thenThrow(new AccountNotFoundException("accountId"));

        assertThatThrownBy(() -> accountService.deleteAccountById("accountId", "customerId"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account can not found by id: " + "accountId");

        Mockito.verify(accountValidateService).retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId");
        Mockito.verifyNoInteractions(transactionRepository);
    }
}

