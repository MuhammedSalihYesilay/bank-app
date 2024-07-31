package com.example.bank_app.dto.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountDto {

    private String id;
    private String customerId;
    private String accountNumber;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
