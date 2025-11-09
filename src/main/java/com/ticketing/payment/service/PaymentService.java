package com.ticketing.payment.service;

import com.ticketing.payment.dto.ChargeRequest;
import com.ticketing.payment.dto.PaymentResponse;
import com.ticketing.payment.dto.RefundRequest;
import com.ticketing.payment.entity.Payment;
import com.ticketing.payment.entity.PaymentStatus;
import com.ticketing.payment.exception.BadRequestException;
import com.ticketing.payment.exception.NotFoundException;
import com.ticketing.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final Random random = new Random();
    
    @Transactional
    public PaymentResponse charge(ChargeRequest request, String idempotencyKey, Long userId) {
        // Check idempotency - return existing payment if key already exists
        Payment existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existingPayment != null) {
            log.info("Idempotent request detected, returning existing payment for key: {}", idempotencyKey);
            return mapToResponse(existingPayment);
        }
        
        // Create payment record
        Payment payment = new Payment();
        payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setOrderId(request.getOrderId());
        payment.setIdempotencyKey(idempotencyKey);
        payment.setUserId(userId);
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);
        
        // Mock payment gateway processing (90% success rate)
        boolean success = random.nextInt(100) < 90;
        
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId("TXN-" + UUID.randomUUID().toString());
            log.info("Payment SUCCESS for order: {}", request.getOrderId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds (mock failure)");
            log.info("Payment FAILED for order: {}", request.getOrderId());
        }
        
        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }
    
    @Transactional
    public PaymentResponse refund(RefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only successful payments can be refunded");
        }
        
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setFailureReason(request.getReason());
        payment = paymentRepository.save(payment);
        
        log.info("Payment REFUNDED for order: {}", payment.getOrderId());
        return mapToResponse(payment);
    }
    
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return mapToResponse(payment);
    }
    
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for order"));
        return mapToResponse(payment);
    }
    
    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrderId(),
            payment.getAmount(),
            payment.getStatus().name(),
            payment.getTransactionId(),
            payment.getFailureReason(),
            payment.getCreatedAt()
        );
    }
}

