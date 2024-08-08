package com.example.bank_app.controller;

import com.example.bank_app.dto.model.AccountDto;
import com.example.bank_app.dto.request.NewAccountRequest;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.service.AccountService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "integration")
class AccountControllerTest {

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Should add a new account successfully when a valid account DTO is provided")
    void shouldAddNewAccount_WhenAccountDto() throws Exception {
        CustomerEntity customer = new CustomerEntity();
        customer.setId("1");
        customer.setEmail("test@example.com");
        customer.setPassword("password123");

        NewAccountRequest newAccountRequest = new NewAccountRequest();
        newAccountRequest.setInitialBalance(BigDecimal.valueOf(1000));

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 8, 2, 21, 15, 18);

        AccountDto expectedAccountDto = AccountDto.builder()
                .id("1")
                .customerId(customer.getId())
                .accountNumber("1234567812345678")
                .balance(BigDecimal.valueOf(1000))
                .createdAt(fixedDateTime)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));

        when(accountService.addNewAccount(anyString(), any(NewAccountRequest.class))).thenReturn(expectedAccountDto);

        String requestJson = serializeJson(newAccountRequest);

        MvcResult result =  performPostRequest("/accounts", requestJson, fixedDateTime);

        validateAccountDto(result);

        verify(accountService, times(1)).addNewAccount(anyString(), any(NewAccountRequest.class));

        SecurityContextHolder.clearContext();

    }

    private String serializeJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private MvcResult performPostRequest(String url, String requestJson, LocalDateTime fixedDateTime) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header(HttpHeaders.AUTHORIZATION, "Basic dGVzdEBleGFtcGxlLmNvbTpwYXNzd29yZDEyMw=="))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/accounts/1"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.customerId").value("1"))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.accountNumber").value("1234567812345678"))
                .andExpect(jsonPath("$.createdAt").value(fixedDateTime.toString()))
                .andReturn();
    }

    private void validateAccountDto(MvcResult result) throws Exception {
        String responseJson = result.getResponse().getContentAsString();
        AccountDto response = objectMapper.readValue(responseJson, AccountDto.class);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should return all accounts of the authenticated customer")
    void shouldGetAccount_WhenAccountDto() throws Exception {
        String customerId = "customerId";
        CustomerEntity customer = new CustomerEntity();
        customer.setId(customerId);

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 8, 2, 21, 15, 18);

        AccountDto expectedAccountDto = AccountDto.builder()
                .id("1")
                .customerId("customerId")
                .accountNumber("1234567812345678")
                .balance(BigDecimal.valueOf(1000))
                .createdAt(fixedDateTime)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));

        when(accountService.getAllAccounts("customerId")).thenReturn(List.of(expectedAccountDto));

        performGetRequest("/accounts", customerId, fixedDateTime);

        SecurityContextHolder.clearContext();
    }

    private void performGetRequest(String url, String customerId, LocalDateTime fixedDateTime) throws Exception {
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer testToken"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].customerId").value(customerId))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].accountNumber").value("1234567812345678"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].balance").value(1000.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdAt").value(fixedDateTime.toString()));
        // [0] Kullanımı (Liste Erişimi):
    }

   @Test
   @DisplayName("Should return the account details when given accountId and authenticated customer")
    void shouldGetAccountByIdCustomerId_WhenAccountDto() throws Exception {
       String customerId = "customerId";
       String accountId = "1";
       CustomerEntity customer = new CustomerEntity();
       customer.setId(customerId);

       LocalDateTime fixedDateTime = LocalDateTime.of(2024, 8, 2, 21, 15, 18);

       AccountDto expectedAccountDto = AccountDto.builder()
               .id(accountId)
               .customerId(customerId)
               .accountNumber("1234567812345678")
               .balance(BigDecimal.valueOf(1000))
               .createdAt(fixedDateTime)
               .build();

       SecurityContextHolder.getContext().setAuthentication(
               new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));

       when(accountService.getAccountByIdAndCustomerId("1", "customerId")).thenReturn(expectedAccountDto);

       performGetAccountByIdRequest("/accounts/" + accountId, customerId, accountId, fixedDateTime);

       SecurityContextHolder.clearContext();
    }

    private void performGetAccountByIdRequest(String url, String customerId, String accountId, LocalDateTime fixedDateTime) throws Exception {
        String token = generateToken();
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.accountNumber").value("1234567812345678"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.createdAt").value(fixedDateTime.toString()));
    }

    private String generateToken() {
        return "Bearer testToken"; // dinamik olarak oluşturma!
    }

    @Test
    @DisplayName("Should delete account by ID for authenticated customer")
    void shouldDeleteAccountById_WhenAccountDto() throws Exception {
        String customerId = "customerId";
        String accountId = "1";

        CustomerEntity customer = new CustomerEntity();
        customer.setId(customerId);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));

        doNothing().when(accountService).deleteAccountById(accountId, customerId);

        performDeleteRequest("/accounts/" + accountId);

        verify(accountService).deleteAccountById(accountId, customerId);

        SecurityContextHolder.clearContext();
    }

    private void performDeleteRequest(String url) throws Exception {
        mockMvc.perform(delete(url)
                        .header("Authorization", "Bearer testToken"))
                .andExpect(status().isNoContent());
    }
}