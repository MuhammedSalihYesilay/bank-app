package com.example.bank_app.controller;


import com.example.bank_app.dto.request.CustomerRegisterRequest;
import com.example.bank_app.dto.response.RegisterResponse;
import com.example.bank_app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse registerCustomer(@RequestBody CustomerRegisterRequest customerRegisterRequest) {
        authService.registerCustomer(customerRegisterRequest);
        return new RegisterResponse();
    }

}

