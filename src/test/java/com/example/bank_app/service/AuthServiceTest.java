package com.example.bank_app.service;

import com.example.bank_app.dto.request.CustomerRegisterRequest;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.exception.EmailAlreadyExistsException;
import com.example.bank_app.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomerRepository customerRepository;

    @Captor
    ArgumentCaptor<CustomerEntity> customerEntityArgumentCaptor;

    @InjectMocks
    private AuthService authService;

    @Test
    void whenRegisterCustomerCalledWithValidRequest_shouldCreateValidUserEntity(){

        CustomerRegisterRequest customerRegisterRequest = new CustomerRegisterRequest();
        customerRegisterRequest.setEmail("test@test.com");
        customerRegisterRequest.setPassword("testPassword");

        when(customerRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");

        authService.registerCustomer(customerRegisterRequest);

        verify(customerRepository).findByEmail("test@test.com");
        verify(passwordEncoder).encode("testPassword");
        verify(customerRepository).save(customerEntityArgumentCaptor.capture());

        CustomerEntity capturedCustomerEntity = customerEntityArgumentCaptor.getValue();

        assertThat(capturedCustomerEntity.getEmail()).isEqualTo("test@test.com");
        assertThat(capturedCustomerEntity.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void whenRegisterCustomerCalledWithInvalidRequest_shouldThrowException(){

        CustomerRegisterRequest customerRegisterRequest = new CustomerRegisterRequest();
        customerRegisterRequest.setEmail("test@test.com");
        customerRegisterRequest.setPassword("testPassword");

        CustomerEntity existingCustomer  = CustomerEntity
                .builder()
                .id("1")
                .email("test@test.com")
                .password("testPassword")
                .build();

        when(customerRepository.findByEmail(customerRegisterRequest.getEmail())).thenReturn(Optional.of(existingCustomer));

        assertThatThrownBy(() -> authService.registerCustomer(customerRegisterRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email "+ customerRegisterRequest.getEmail() + " already exists");

        verify(customerRepository).findByEmail(customerRegisterRequest.getEmail());
        verifyNoInteractions(passwordEncoder);

        verify(customerRepository, never()).save(Mockito.any(CustomerEntity.class));
    }
}