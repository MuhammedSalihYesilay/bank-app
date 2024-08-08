package com.example.bank_app.controller;

import com.example.bank_app.dto.model.CustomerDto;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "integration")
class CustomerControllerTest {

    @MockBean
    private CustomerService customerService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return authenticated customer details")
    void shouldGetAuthenticatedCustomerById_WhenCustomer()  throws Exception {
        String customerId = "1";

        CustomerEntity principal = new CustomerEntity();
        principal.setId(customerId);
        principal.setEmail("test@example.com");

        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(customerId);
        customerDto.setEmail("test@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(customerService.getAuthenticatedCustomerById( "1")).thenReturn(customerDto);

        performGetRequest("/customers/me", customerId);

        verify(customerService, times(1)).getAuthenticatedCustomerById( "1");

        SecurityContextHolder.clearContext();
    }

    private void performGetRequest(String url, String customerId) throws Exception {
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value( customerId))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}