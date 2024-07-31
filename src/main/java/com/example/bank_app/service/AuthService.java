package com.example.bank_app.service;

import com.example.bank_app.dto.request.CustomerRegisterRequest;
import com.example.bank_app.exception.EmailAlreadyExistsException;
import com.example.bank_app.repository.CustomerRepository;
import com.example.bank_app.entity.CustomerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerCustomer(CustomerRegisterRequest customerRegisterRequest) {
        EmailAlreadyExistException(customerRegisterRequest);

        CustomerEntity customerEntity = CustomerEntity
                .builder()
                .email(customerRegisterRequest.getEmail())
                .firstName(customerRegisterRequest.getFirstName())
                .lastName(customerRegisterRequest.getLastName())
                .password(passwordEncoder.encode(customerRegisterRequest.getPassword()))
                .phoneNumber(customerRegisterRequest.getPhoneNumber())
                .dateOfBirth(customerRegisterRequest.getDateOfBirth())
                .build();

        customerRepository.save(customerEntity);
    }

    private void EmailAlreadyExistException(CustomerRegisterRequest customerRegisterRequest) {
        Optional<CustomerEntity> optionalCustomer = customerRepository.findByEmail(customerRegisterRequest.getEmail());

        if (optionalCustomer.isPresent()) {
            throw new EmailAlreadyExistsException(customerRegisterRequest.getEmail());
        }
    }


}