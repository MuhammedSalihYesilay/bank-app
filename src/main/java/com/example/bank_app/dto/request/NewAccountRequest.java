package com.example.bank_app.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewAccountRequest {

    private BigDecimal initialBalance;
}
