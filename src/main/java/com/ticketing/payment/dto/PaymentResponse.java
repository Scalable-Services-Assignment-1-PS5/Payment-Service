package com.ticketing.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String orderId;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime createdAt;
}

