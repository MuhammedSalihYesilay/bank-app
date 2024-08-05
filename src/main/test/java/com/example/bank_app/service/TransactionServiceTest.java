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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.internal.verification.Times;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountValidateService accountValidateService;

    @Captor
    private ArgumentCaptor<AccountEntity> accountCaptor;

    @Captor
    private ArgumentCaptor<TransactionEntity> transactionCaptor;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("Should add a new transaction when accounts found by account id and customer id and sender balance enough.")
    public void shouldAddNewTransaction_whenAccountsFoundByAccountIdAndCustomerIdAndSenderBalanceEnough() {
        TransactionType transactionType = TransactionType.TRANSFER;

        AccountEntity senderAccount = AccountEntity
                .builder()
                .id("senderAccountId")
                .customerId("senderCustomerId")
                .accountNumber("senderAccountNumber")
                .balance(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .build();

        AccountEntity receiverAccount = AccountEntity
                .builder()
                .id("receiverAccountId")
                .customerId("receiverCustomerId")
                .accountNumber("receiverAccountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        NewMoneyTransferRequest newMoneyTransferRequest = NewMoneyTransferRequest
                .builder()
                .amount(BigDecimal.TEN)
                .receiverAccountNumber("receiverAccountNumber")
                .description("description")
                .build();

        TransactionEntity transactionEntity = TransactionEntity
                .builder()
                .id("transactionId")
                .transactionType(transactionType)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .amount(newMoneyTransferRequest.getAmount())
                .description(newMoneyTransferRequest.getDescription())
                .build();

        TransactionDto expected = TransactionDto
                .builder()
                .id("transactionId")
                .transactionType(transactionType)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .amount(newMoneyTransferRequest.getAmount())
                .description(newMoneyTransferRequest.getDescription())
                .build();

        Mockito.when(accountRepository.findByIdAndCustomerId("senderAccountId", "senderCustomerId")).thenReturn(Optional.of(senderAccount));
        Mockito.when(accountRepository.findByAccountNumber("receiverAccountNumber")).thenReturn(Optional.of(receiverAccount));
        Mockito.when(transactionRepository.save(transactionCaptor.capture())).thenReturn(transactionEntity);
        Mockito.when(modelMapper.map(transactionEntity, TransactionDto.class)).thenReturn(expected);

        TransactionDto result = transactionService.addNewTransaction(senderAccount.getCustomerId(), senderAccount.getId(), newMoneyTransferRequest);

        assertEquals(expected, result);

        verify(accountRepository).findByIdAndCustomerId("senderAccountId", "senderCustomerId");
        verify(accountRepository).findByAccountNumber("receiverAccountNumber");
        verify(accountRepository, new Times(2)).save(accountCaptor.capture());

        List<AccountEntity> capturedAccounts = accountCaptor.getAllValues();
        assertThat(capturedAccounts).hasSize(2);

        AccountEntity capturedSenderAccount = accountCaptor.getAllValues().getFirst();
        assertThat(capturedSenderAccount.getId()).isEqualTo(senderAccount.getId());
        assertThat(capturedSenderAccount.getBalance()).isEqualTo(BigDecimal.valueOf(90));

        AccountEntity capturedReceiverAccount = accountCaptor.getAllValues().getLast();
        assertThat(capturedReceiverAccount.getId()).isEqualTo(receiverAccount.getId());
        assertThat(capturedReceiverAccount.getBalance()).isEqualTo(BigDecimal.TEN);

        verify(transactionRepository).save(Mockito.any(TransactionEntity.class));

        TransactionEntity capturedTransaction = transactionCaptor.getValue();
        assertThat(capturedTransaction.getDescription()).isEqualTo(newMoneyTransferRequest.getDescription());
        assertThat(capturedTransaction.getTransactionType()).isEqualTo(transactionType);
        assertThat(capturedTransaction.getReceiverAccountId()).isEqualTo(capturedReceiverAccount.getId());
        assertThat(capturedTransaction.getSenderAccountId()).isEqualTo(capturedSenderAccount.getId());
        assertThat(capturedTransaction.getAmount()).isEqualTo(newMoneyTransferRequest.getAmount());

        verify(modelMapper).map(transactionEntity, TransactionDto.class);
    }

    @Test
    @DisplayName("Should not add a new transaction when sender account not found by account id and customer id.")
    public void shouldNotAddNewTransaction_whenSenderAccountNotFoundByAccountIdAndCustomerId() {
        when(accountRepository.findByIdAndCustomerId("senderAccountId", "senderCustomerId")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.addNewTransaction("senderCustomerId", "senderAccountId", eq(Mockito.any(NewMoneyTransferRequest.class))))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account can not found by id: " + "senderAccountId");

        verify(accountRepository).findByIdAndCustomerId("senderAccountId", "senderCustomerId");
        Mockito.verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should not add a new transaction when accounts found by account id and customer id but sender balance is not enough.")
    public void shouldNotAddNewTransaction_whenAccountsFoundByAccountIdAndCustomerIdButSenderBalanceIsNotEnough() {
        NewMoneyTransferRequest newMoneyTransferRequest = NewMoneyTransferRequest
                .builder()
                .amount(BigDecimal.TEN)
                .receiverAccountNumber("receiverAccountNumber")
                .description("description")
                .build();

        AccountEntity senderAccount = AccountEntity
                .builder()
                .id("senderAccountId")
                .customerId("senderCustomerId")
                .accountNumber("senderAccountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        AccountEntity receiverAccount = AccountEntity
                .builder()
                .id("receiverAccountId")
                .customerId("receiverCustomerId")
                .accountNumber("receiverAccountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findByIdAndCustomerId("senderAccountId", "senderCustomerId")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByAccountNumber("receiverAccountNumber")).thenReturn(Optional.of(receiverAccount));

        assertThatThrownBy(() -> transactionService.addNewTransaction("senderCustomerId", "senderAccountId", newMoneyTransferRequest))
                .isInstanceOf(BalanceIsNotEnoughException.class)
                .hasMessageContaining("Balance is not enough for this transfer! Your balance: " + senderAccount.getBalance());

        verify(accountRepository).findByIdAndCustomerId("senderAccountId", "senderCustomerId");
        verify(accountRepository).findByAccountNumber("receiverAccountNumber");
        Mockito.verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should not add a new transaction when accounts found by account id and customer id but transaction amount is invalid.")
    public void shouldNotAddNewTransaction_whenAccountsFoundByAccountIdAndCustomerIdButTransactionAmountIsInvalid() {
        NewMoneyTransferRequest newMoneyTransferRequest = NewMoneyTransferRequest
                .builder()
                .amount(BigDecimal.valueOf(-10))
                .receiverAccountNumber("receiverAccountNumber")
                .description("description")
                .build();

        AccountEntity senderAccount = AccountEntity
                .builder()
                .id("senderAccountId")
                .customerId("senderCustomerId")
                .accountNumber("senderAccountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        AccountEntity receiverAccount = AccountEntity
                .builder()
                .id("receiverAccountId")
                .customerId("receiverCustomerId")
                .accountNumber("receiverAccountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findByIdAndCustomerId("senderAccountId", "senderCustomerId")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByAccountNumber("receiverAccountNumber")).thenReturn(Optional.of(receiverAccount));

        assertThatThrownBy(() -> transactionService.addNewTransaction("senderCustomerId", "senderAccountId", newMoneyTransferRequest))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("Invalid amount! Please enter a value greater than 0.");

        verify(accountRepository).findByIdAndCustomerId("senderAccountId", "senderCustomerId");
        verify(accountRepository).findByAccountNumber("receiverAccountNumber");
        Mockito.verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should not add a new transaction when the sender account is present and balance is enough but the receiver account not found.")
    public void shouldNotAddNewTransaction_whenTheSenderAccountIsPresentAndBalanceIsEnoughButTheReceiverAccountNotFound() {
        AccountEntity senderAccount = AccountEntity
                .builder()
                .id("senderAccountId")
                .customerId("senderCustomerId")
                .accountNumber("senderAccountNumber")
                .balance(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .build();

        NewMoneyTransferRequest newMoneyTransferRequest = NewMoneyTransferRequest
                .builder()
                .amount(BigDecimal.TEN)
                .receiverAccountNumber("receiverAccountNumber")
                .description("description")
                .build();

        Mockito.when(accountRepository.findByIdAndCustomerId("senderAccountId","senderCustomerId" )).thenReturn(Optional.of(senderAccount));
        Mockito.when(accountRepository.findByAccountNumber(newMoneyTransferRequest.getReceiverAccountNumber())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.addNewTransaction("senderCustomerId", "senderAccountId", newMoneyTransferRequest))
                .isInstanceOf(AccountNotFoundByNumberException.class)
                .hasMessageContaining("Account can not found by number: " + newMoneyTransferRequest.getReceiverAccountNumber());

        verify(accountRepository).findByIdAndCustomerId("senderAccountId","senderCustomerId" );
        verify(accountRepository).findByAccountNumber(newMoneyTransferRequest.getReceiverAccountNumber());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Should return all transactions when the account found by account id and customer id.")
    public void shouldReturnAllTransactions_whenTheAccountFoundByAccountIdAndCustomerId() {
        AccountEntity accountEntity = AccountEntity
                .builder()
                .id("accountId")
                .customerId("customerId")
                .accountNumber("accountNumber")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        TransactionEntity transactionEntity1 = TransactionEntity
                .builder()
                .id("transactionId1")
                .senderAccountId("accountId")
                .receiverAccountId("receiverAccountId")
                .amount(BigDecimal.valueOf(100))
                .transactionType(TransactionType.TRANSFER)
                .date(LocalDateTime.now())
                .description("description")
                .build();

        TransactionEntity transactionEntity2 = TransactionEntity
                .builder()
                .id("transactionId2")
                .senderAccountId("senderAccountId")
                .receiverAccountId("accountId")
                .amount(BigDecimal.valueOf(200))
                .transactionType(TransactionType.TRANSFER)
                .date(LocalDateTime.now())
                .description("description")
                .build();

        TransactionDto transactionDto1 = TransactionDto
                .builder()
                .id("transactionId1")
                .senderAccountId("accountId")
                .receiverAccountId("receiverAccountId")
                .amount(BigDecimal.valueOf(100))
                .transactionType(TransactionType.TRANSFER)
                .date(LocalDateTime.now())
                .description("description")
                .build();

        TransactionDto transactionDto2 = TransactionDto
                .builder()
                .id("transactionId2")
                .senderAccountId("senderAccountId")
                .receiverAccountId("accountId")
                .amount(BigDecimal.valueOf(200))
                .transactionType(TransactionType.TRANSFER)
                .date(LocalDateTime.now())
                .description("description")
                .build();

        when(accountValidateService.retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId")).thenReturn((accountEntity));
        when(transactionRepository.findAllByAccountId("accountId")).thenReturn(Arrays.asList(transactionEntity1,transactionEntity2));
        when(modelMapper.map(transactionEntity1, TransactionDto.class)).thenReturn(transactionDto1);
        when(modelMapper.map(transactionEntity2, TransactionDto.class)).thenReturn(transactionDto2);


        List<TransactionDto> result = transactionService.getAllTransactions("accountId", "customerId");
        List<TransactionDto> expected = Arrays.asList(transactionDto1, transactionDto2);

        assertEquals(expected, result);

        verify(accountValidateService).retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId");
        verify(transactionRepository).findAllByAccountId("accountId");
        verify(modelMapper).map(transactionEntity1, TransactionDto.class);
        verify(modelMapper).map(transactionEntity2, TransactionDto.class);

    }

    @Test
    @DisplayName("Should not return all transactions when the account not found by account id and customer id.")
    public void shouldNotReturnAllTransactions_whenTheAccountNotFoundByAccountIdAndCustomerId() {
        when(accountValidateService.retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId"))
                .thenThrow(new AccountNotFoundException("accountId"));

        assertThatThrownBy(() -> transactionService.getAllTransactions("accountId", "customerId"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account can not found by id: " + "accountId");

        verify(accountValidateService).retrieveAndValidateAccountByIdAndCustomerId("accountId", "customerId");
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(modelMapper);
    }

}
