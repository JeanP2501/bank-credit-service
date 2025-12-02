package com.bank.credit.mapper;

import com.bank.credit.model.dto.CreditRequest;
import com.bank.credit.model.dto.CreditResponse;
import com.bank.credit.model.entity.Credit;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Credit entity and DTOs
 */
@Component
public class CreditMapper {

    /**
     * Convert CreditRequest to Credit entity
     * @param request the credit request
     * @return Credit entity
     */
    public Credit toEntity(CreditRequest request) {
        return Credit.builder()
                .creditNumber(generateCreditNumber())
                .creditType(request.getCreditType())
                .customerId(request.getCustomerId())
                .creditAmount(request.getCreditAmount())
                .balance(java.math.BigDecimal.ZERO)
                .creditLimit(request.getCreditAmount()) // Initially, limit equals amount
                .interestRate(request.getInterestRate() != null ? request.getInterestRate() : java.math.BigDecimal.ZERO)
                .minimumPayment(request.getMinimumPayment())
                .paymentDueDay(request.getPaymentDueDay())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Convert Credit entity to CreditResponse
     * @param credit the credit entity
     * @return CreditResponse DTO
     */
    public CreditResponse toResponse(Credit credit) {
        return CreditResponse.builder()
                .id(credit.getId())
                .creditNumber(credit.getCreditNumber())
                .creditType(credit.getCreditType())
                .customerId(credit.getCustomerId())
                .creditAmount(credit.getCreditAmount())
                .balance(credit.getBalance())
                .creditLimit(credit.getCreditLimit())
                .availableCredit(credit.getAvailableCredit())
                .interestRate(credit.getInterestRate())
                .minimumPayment(credit.getMinimumPayment())
                .paymentDueDay(credit.getPaymentDueDay())
                .active(credit.getActive())
                .createdAt(credit.getCreatedAt())
                .updatedAt(credit.getUpdatedAt())
                .build();
    }

    /**
     * Update existing Credit entity with request data
     * @param credit the existing credit
     * @param request the update request
     */
    public void updateEntity(Credit credit, CreditRequest request) {
        credit.setInterestRate(request.getInterestRate());
        credit.setMinimumPayment(request.getMinimumPayment());
        credit.setPaymentDueDay(request.getPaymentDueDay());
        credit.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Generate unique credit number
     * @return credit number
     */
    private String generateCreditNumber() {
        // Format: CRD-XXXXXXXXXX (10 random digits)
        return "CRD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
