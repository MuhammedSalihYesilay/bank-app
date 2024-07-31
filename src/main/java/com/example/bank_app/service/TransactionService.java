package com.example.bank_app.service;

import com.example.bank_app.common.TransactionType;
import com.example.bank_app.dto.model.TransactionDto;
import com.example.bank_app.dto.request.NewMoneyTransferRequest;
import com.example.bank_app.entity.AccountEntity;
import com.example.bank_app.entity.TransactionEntity;
import com.example.bank_app.exception.AccountNotFoundByNumberException;
import com.example.bank_app.exception.AccountNotFoundException;
import com.example.bank_app.exception.BalanceIsNotEnoughException;
import com.example.bank_app.exception.InvalidAmountException;
import com.example.bank_app.repository.AccountRepository;
import com.example.bank_app.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountValidateService accountValidateService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public TransactionDto addNewTransaction(String senderCustomerId, String senderAccountId, NewMoneyTransferRequest newMoneyTransferRequest) {
        AccountEntity senderAccount = getValidatedSenderAccountByCustomerIdAndAccountId(senderAccountId, senderCustomerId);
        AccountEntity receiverAccount = getValidatedReceiverAccountByAccountNumber(newMoneyTransferRequest.getReceiverAccountNumber());
        validateSenderBalanceEnoughAndValidAmount(senderAccount.getBalance(), newMoneyTransferRequest.getAmount());
        TransactionEntity transactionEntity = createTransaction(senderAccountId, receiverAccount.getId(), newMoneyTransferRequest.getAmount(), newMoneyTransferRequest.getDescription());
        updateBalances(senderAccount, receiverAccount, newMoneyTransferRequest);

        return modelMapper.map(transactionEntity, TransactionDto.class);
    }

    private AccountEntity getValidatedSenderAccountByCustomerIdAndAccountId(String senderAccountId, String senderCustomerId) {
        return accountRepository.findByIdAndCustomerId(senderAccountId, senderCustomerId)
                .orElseThrow(() -> new AccountNotFoundException(senderAccountId));
    }

    private AccountEntity getValidatedReceiverAccountByAccountNumber(String receiverAccountNumber) {
        return accountRepository.findByAccountNumber(receiverAccountNumber)
                .orElseThrow(() -> new AccountNotFoundByNumberException(receiverAccountNumber));
    }

    private void validateSenderBalanceEnoughAndValidAmount(BigDecimal balance, BigDecimal amount) {
        if(balance.compareTo(amount) < 0) {
            throw new BalanceIsNotEnoughException(balance);
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
    }

    private void updateBalances(AccountEntity senderAccount, AccountEntity receiverAccount, NewMoneyTransferRequest newMoneyTransferRequest) {
        senderAccount.setBalance(senderAccount.getBalance().subtract(newMoneyTransferRequest.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(newMoneyTransferRequest.getAmount()));

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
    }

    private TransactionEntity createTransaction(String senderAccountId, String receiverAccountId, BigDecimal amount, String description) {
        TransactionEntity transaction = TransactionEntity
                .builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .transactionType(TransactionType.TRANSFER)
                .amount(amount)
                .description(description)
                .build();

        return transactionRepository.save(transaction);
    }

    public List<TransactionDto> getAllTransactions(String accountId, String customerId) {
        accountValidateService.retrieveAndValidateAccountByIdAndCustomerId(accountId, customerId);

        List<TransactionEntity> transactions = transactionRepository.findAllByAccountId(accountId);

        return  transactions.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDto.class))
                .collect(Collectors.toList());
    }
}
