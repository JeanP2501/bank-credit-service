package com.bank.credit.model.entity;

import com.bank.credit.model.enums.CreditType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Credit entity representing credit products (active products)
 * Supports personal loans, business loans, and credit cards
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "credits")
public class Credit {

    @Id
    private String id;

    @NotBlank(message = "Credit number is required")
    private String creditNumber;

    @NotNull(message = "Credit type is required")
    private CreditType creditType;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    /**
     * Total credit amount approved
     */
    @NotNull(message = "Credit amount is required")
    @Positive(message = "Credit amount must be positive")
    private BigDecimal creditAmount;

    /**
     * Current outstanding balance
     */
    @NotNull(message = "Balance is required")
    @PositiveOrZero(message = "Balance must be zero or positive")
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Available credit limit (for credit cards)
     * creditLimit = creditAmount - balance
     */
    @PositiveOrZero(message = "Credit limit must be zero or positive")
    private BigDecimal creditLimit;

    /**
     * Monthly interest rate (percentage)
     */
    @PositiveOrZero(message = "Interest rate must be zero or positive")
    @Builder.Default
    private BigDecimal interestRate = BigDecimal.ZERO;

    /**
     * Minimum monthly payment amount
     */
    @PositiveOrZero(message = "Minimum payment must be zero or positive")
    private BigDecimal minimumPayment;

    /**
     * Payment due day of month (1-31)
     */
    private Integer paymentDueDay;

    /**
     * Indicates if credit is currently active
     */
    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    /**
     * Calculate available credit (mainly for credit cards)
     * @return available credit amount
     */
    public BigDecimal getAvailableCredit() {
        if (creditLimit != null) {
            return creditLimit;
        }
        return creditAmount.subtract(balance);
    }

    /**
     * Check if credit has available balance
     * @return true if has available credit
     */
    public boolean hasAvailableCredit() {
        return getAvailableCredit().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if this is a credit card
     * @return true if credit type is CREDIT_CARD
     */
    public boolean isCreditCard() {
        return creditType == CreditType.CREDIT_CARD;
    }
}
