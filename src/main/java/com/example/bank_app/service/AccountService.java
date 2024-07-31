package com.example.bank_app.service;

import com.example.bank_app.common.TransactionType;
import com.example.bank_app.dto.model.AccountDto;
import com.example.bank_app.dto.request.NewAccountRequest;
import com.example.bank_app.entity.AccountEntity;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.entity.TransactionEntity;
import com.example.bank_app.exception.*;
import com.example.bank_app.repository.AccountRepository;
import com.example.bank_app.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final TransactionRepository transactionRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final AccountValidateService accountValidateService;

    public AccountDto addNewAccount(String customerId, NewAccountRequest newAccountRequest){
        validateInitialBalance(newAccountRequest.getInitialBalance());

        AccountEntity accountEntity = createAccount(customerId, newAccountRequest.getInitialBalance());
        AccountEntity savedAccount = accountRepository.save(accountEntity);

        createInitialTransactionIfNecessary(savedAccount, newAccountRequest.getInitialBalance());

        return modelMapper.map(savedAccount, AccountDto.class);
    }

    private void createInitialTransactionIfNecessary(AccountEntity account, BigDecimal initialBalance) {
        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            createInitialTransactionAndSave(account, initialBalance);
        }
    }

    private void createInitialTransactionAndSave(AccountEntity account, BigDecimal initialBalance) {
        TransactionEntity transaction = TransactionEntity
                .builder()
                .transactionType(TransactionType.INITIAL)
                .senderAccountId(account.getId())
                .receiverAccountId(account.getId())
                .amount(initialBalance)
                .build();
        transactionRepository.save(transaction);
    }

    private String generateAccountNumber() {
        return IntStream.range(0,16)
                .mapToObj(i -> String.valueOf(secureRandom.nextInt(10)))
                .collect(Collectors.joining());
    }

    private void validateInitialBalance(BigDecimal initialBalance) {
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountCreationException();
        }
    }

    private AccountEntity createAccount(String customerId, BigDecimal initialBalance) {
        return AccountEntity
                .builder()
                .accountNumber(generateAccountNumber())
                .balance(initialBalance)
                .customerId(customerId)
                .build();
    }

    public List<AccountDto> getAllAccounts(String customerId) {
        List<AccountEntity> accounts = accountRepository.findAllByCustomerId(customerId);
        return mapAccountsToAccountDto(accounts);
    }

    private List<AccountDto> mapAccountsToAccountDto(List<AccountEntity> accounts) {
        return accounts.stream()
                .map(account -> modelMapper.map(account, AccountDto.class))
                .collect(Collectors.toList());
    }

    public AccountDto getAccountByIdAndCustomerId(String accountId, String customerId) {
        AccountEntity accountEntity = accountValidateService.retrieveAndValidateAccountByIdAndCustomerId(accountId, customerId);
        return modelMapper.map(accountEntity, AccountDto.class);
    }

    @Transactional
    public void deleteAccountById(String accountId, String customerId) {
        AccountEntity accountEntity = accountValidateService.retrieveAndValidateAccountByIdAndCustomerId(accountId, customerId);
        deleteInitialTransactionIfExist(accountId);
        accountRepository.delete(accountEntity);
    }

    private void deleteInitialTransactionIfExist(String accountId) {
        Optional<TransactionEntity> initialTransaction = transactionRepository.findInitialTransactionByAccountId(accountId);
        initialTransaction.ifPresent(transactionRepository::delete);
    }
}
