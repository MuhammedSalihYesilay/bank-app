package com.example.bank_app.controller;

import com.example.bank_app.dto.request.CustomerRegisterRequest;
import com.example.bank_app.dto.response.RegisterResponse;
import com.example.bank_app.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "integration")
class AuthControllerTest {

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;
    // objectMapper: JSON verilerini Java nesnelerine ve tam tersine dönüştürmek için kullanılan bir yardımcı sınıf.

    @Autowired
    MockMvc mockMvc;
    // mockMvc: HTTP isteklerini simüle ederek kontrolcüleri test etmek için kullanılan bir yardımcı sınıf.

    @Test
    @DisplayName("Should register a customer successfully when valid data is provided")
    void shouldRegisterCustomerSuccessfully() throws Exception {
        CustomerRegisterRequest customerRegisterRequest = new CustomerRegisterRequest();
        customerRegisterRequest.setEmail("test@example.com");
        customerRegisterRequest.setPassword("customer-123");

        String requestJson = serializeJson(customerRegisterRequest);

        doNothing().when(authService).registerCustomer(any(CustomerRegisterRequest.class));

        MvcResult result = performPostRequest(requestJson);

        validateRegisterResponse(result);
    }

    private String serializeJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private MvcResult performPostRequest(String requestJson) throws Exception {
        return mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists())
                .andReturn();
    }

    private void validateRegisterResponse(MvcResult result) throws Exception {
        String responseJson = result.getResponse().getContentAsString();
        RegisterResponse response = objectMapper.readValue(responseJson, RegisterResponse.class);
        assertThat(response).isNotNull();
    }
}