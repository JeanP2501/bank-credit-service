package com.bank.credit.model.dto;

import com.bank.credit.model.enums.CreditType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for credit creation and update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequest {

    @NotNull(message = "Credit type is required")
    private CreditType creditType;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Credit amount is required")
    @Positive(message = "Credit amount must be positive")
    private BigDecimal creditAmount;

    @PositiveOrZero(message = "Interest rate must be zero or positive")
    private BigDecimal interestRate;

    @PositiveOrZero(message = "Minimum payment must be zero or positive")
    private BigDecimal minimumPayment;

    @Min(value = 1, message = "Payment due day must be between 1 and 31")
    @Max(value = 31, message = "Payment due day must be between 1 and 31")
    private Integer paymentDueDay;
}
