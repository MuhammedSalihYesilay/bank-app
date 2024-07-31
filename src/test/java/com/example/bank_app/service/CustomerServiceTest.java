package com.example.bank_app.service;

import com.example.bank_app.dto.model.CustomerDto;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {


    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CustomerService customerService;


    @Test
    void testGetAuthenticatedCustomerById_Success() {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId("customerId");
        customerEntity.setEmail("test@example.com");

        CustomerDto customerDto = new CustomerDto();
        customerDto.setId("customerId");
        customerDto.setEmail("test@example.com");

        when(customerRepository.findById("customerId")).thenReturn(Optional.of(customerEntity));
        when(modelMapper.map(customerEntity, CustomerDto.class)).thenReturn(customerDto);

        CustomerDto result = customerService.getAuthenticatedCustomerById("customerId");

        assertThat(result)
                .isNotNull()
                .returns("customerId", CustomerDto::getId)
                .returns("test@example.com", CustomerDto::getEmail);


        verify(customerRepository, times(1)).findById("customerId");
        verify(modelMapper, times(1)).map(customerEntity, CustomerDto.class);
    }

    @Test
    void testGetAuthenticatedCustomerById_ThrowsUsernameNotFoundException() {
        when(customerRepository.findById("invalidCustomerId")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customerService.getAuthenticatedCustomerById("invalidCustomerId"));

        verify(customerRepository, times(1)).findById("invalidCustomerId");
        verifyNoInteractions(modelMapper);
    }

}