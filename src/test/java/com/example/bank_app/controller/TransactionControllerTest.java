package com.example.bank_app.controller;

import com.example.bank_app.common.TransactionType;
import com.example.bank_app.dto.model.TransactionDto;
import com.example.bank_app.dto.request.NewMoneyTransferRequest;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "integration")
class TransactionControllerTest {

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Add a new transaction and verify response and location header")
    void shouldAddNewTransaction_TransactionDto() throws Exception{
        String accountId = "1";

        CustomerEntity customer = new CustomerEntity();
        customer.setId("1");
        customer.setEmail("test@example.com");

        NewMoneyTransferRequest newMoneyTransferRequest = NewMoneyTransferRequest.builder()
                .amount(BigDecimal.valueOf(1000))
                .description("test description")
                .receiverAccountNumber("test receiver account")
                .build();

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 8, 2, 21, 15, 18);

        TransactionDto expecteTransactionDto = TransactionDto.builder()
                .id("1")
                .senderAccountId("test sender account")
                .amount(BigDecimal.valueOf(1000))
                .description("test description")
                .receiverAccountId("test receiver account")
                .transactionType(TransactionType.TRANSFER)
                .date(fixedDateTime)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));

        when(transactionService.addNewTransaction("1", "1", newMoneyTransferRequest)).thenReturn(expecteTransactionDto);

        String requestJson = serializeJson(newMoneyTransferRequest);

        MvcResult result = performPostRequest("/accounts/" + accountId + "/transfer-money", requestJson, fixedDateTime);

        validateAccountDto(result);

        verify(transactionService, times(1)).addNewTransaction("1", "1", newMoneyTransferRequest);

        SecurityContextHolder.clearContext();
    }

    private String serializeJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private MvcResult performPostRequest(String url, String requestJson, LocalDateTime fixedDateTime) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/1/transfer-money/1"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.senderAccountId").value("test sender account"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.description").value("test description"))
                .andExpect(jsonPath("$.receiverAccountId").value("test receiver account"))
                .andExpect(jsonPath("$.transactionType").value(TransactionType.TRANSFER.name()))
                .andExpect(jsonPath("$.date").value(fixedDateTime.toString()))
                .andReturn();
    }

    private void validateAccountDto(MvcResult result) throws Exception {
        String responseJson = result.getResponse().getContentAsString();
        TransactionDto response = objectMapper.readValue(responseJson, TransactionDto.class);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Retrieve all transactions for a given account and verify response")
    void shouldGetAllTransactions_TransactionDto() throws Exception{
        String accountId = "1";

        CustomerEntity customer = new CustomerEntity();
        customer.setId("1");
        customer.setEmail("test@example.com");

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 8, 2, 21, 15, 18);

        TransactionDto expecteTransactionDto = TransactionDto.builder()
                .id("1")
                .senderAccountId("test sender account")
                .amount(BigDecimal.valueOf(1000))
                .description("test description")
                .receiverAccountId("test receiver account")
                .transactionType(TransactionType.TRANSFER)
                .date(fixedDateTime)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));

        when(transactionService.getAllTransactions("1","1")).thenReturn(List.of(expecteTransactionDto));

        performGetRequest("/accounts/" + accountId + "/transaction-history", accountId, fixedDateTime);

        SecurityContextHolder.clearContext();
    }

    private void performGetRequest(String url, String accountId, LocalDateTime fixedDateTime) throws Exception {
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(accountId))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].senderAccountId").value("test sender account"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amount").value(BigDecimal.valueOf(1000)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("test description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].receiverAccountId").value("test receiver account"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].transactionType").value(TransactionType.TRANSFER.name()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].date").value(fixedDateTime.toString()))
                .andReturn();
    }
}