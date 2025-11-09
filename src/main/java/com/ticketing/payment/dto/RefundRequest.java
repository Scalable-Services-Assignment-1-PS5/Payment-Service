package com.ticketing.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundRequest {
    @NotNull(message = "Payment ID is required")
    private Long paymentId;
    
    private String reason;
}

