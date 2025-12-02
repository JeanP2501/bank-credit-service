package com.bank.credit.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when there is insufficient credit available
 */
public class InsufficientCreditException extends RuntimeException {

    public InsufficientCreditException(BigDecimal requested, BigDecimal available) {
        super(String.format("Insufficient credit. Requested: %s, Available: %s",
                requested, available));
    }
}
