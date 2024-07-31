package com.example.bank_app.service;


import com.example.bank_app.dto.model.CustomerDto;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.exception.CustomerNotFoundException;
import com.example.bank_app.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    public CustomerDto getAuthenticatedCustomerById(String customerId) {
        CustomerEntity customerEntity = retrieveCustomerAndValidateById(customerId);
        return modelMapper.map(customerEntity, CustomerDto.class);
    }

    private CustomerEntity retrieveCustomerAndValidateById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException(customerId));
    }
}

