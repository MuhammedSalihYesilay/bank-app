package com.example.bank_app.dto.model;

import com.example.bank_app.common.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDto {

    private String id;
    private String senderAccountId;
    private String receiverAccountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private LocalDateTime date;
    private String description;
}
