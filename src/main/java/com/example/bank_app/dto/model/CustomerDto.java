package com.example.bank_app.dto.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerDto {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDateTime dateOfBirth;
}
