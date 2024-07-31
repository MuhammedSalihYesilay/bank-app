package com.example.bank_app.controller;



import com.example.bank_app.dto.model.CustomerDto;
import com.example.bank_app.entity.CustomerEntity;
import com.example.bank_app.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("customers")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("me")
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto getAuthenticatedCustomerById() {
        CustomerEntity principal = (CustomerEntity)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customerService.getAuthenticatedCustomerById(principal.getId());
    }
}
