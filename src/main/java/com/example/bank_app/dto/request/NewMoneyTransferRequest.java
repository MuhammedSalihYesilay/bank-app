package com.example.bank_app.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class NewMoneyTransferRequest {

    private BigDecimal amount;
    private String receiverAccountNumber;
    private String description;
}
