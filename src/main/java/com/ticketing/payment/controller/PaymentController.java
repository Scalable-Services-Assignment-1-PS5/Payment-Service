package com.ticketing.payment.controller;

import com.ticketing.payment.dto.ChargeRequest;
import com.ticketing.payment.dto.PaymentResponse;
import com.ticketing.payment.dto.RefundRequest;
import com.ticketing.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/charge")
    @Operation(summary = "Charge payment", description = "Process payment charge with idempotency support")
    public ResponseEntity<PaymentResponse> charge(
            @Valid @RequestBody ChargeRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(paymentService.charge(request, idempotencyKey, userId));
    }
    
    @PostMapping("/refund")
    @Operation(summary = "Refund payment", description = "Refund a successful payment")
    public ResponseEntity<PaymentResponse> refund(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refund(request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieve payment details by ID")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID", description = "Retrieve payment details by order ID")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}

