package com.example.bank_app.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerRegisterRequest {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDateTime dateOfBirth;
}
