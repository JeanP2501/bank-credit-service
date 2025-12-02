package com.bank.credit.service;

import com.bank.credit.client.CustomerClient;
import com.bank.credit.exception.BusinessRuleException;
import com.bank.credit.exception.CreditNotFoundException;
import com.bank.credit.exception.CustomerNotFoundException;
import com.bank.credit.exception.InsufficientCreditException;
import com.bank.credit.mapper.CreditMapper;
import com.bank.credit.model.dto.*;
import com.bank.credit.model.enums.CreditType;
import com.bank.credit.model.enums.CustomerType;
import com.bank.credit.repository.CreditRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service layer for Credit operations
 * Implements business logic and rules for credit management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditRepository creditRepository;
    private final CreditMapper creditMapper;
    private final CustomerClient customerClient;

    /**
     * Create a new credit with business rule validations
     * @param request the credit request
     * @return Mono of CreditResponse
     */
    public Mono<CreditResponse> create(CreditRequest request) {
        log.debug("Creating credit for customer: {}", request.getCustomerId());

        return customerClient.getCustomerById(request.getCustomerId())
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(request.getCustomerId())))
                .flatMap(customer -> validateBusinessRules(request, customer))
                .map(creditMapper::toEntity)
                .flatMap(creditRepository::save)
                .doOnSuccess(credit -> log.info("Credit created successfully: {}", credit.getCreditNumber()))
                .map(creditMapper::toResponse);
    }

    /**
     * Validate business rules for credit creation
     */
    private Mono<CreditRequest> validateBusinessRules(CreditRequest request, CustomerResponse customer) {
        log.debug("Validating business rules for customer type: {} and credit type: {}",
                customer.getCustomerType(), request.getCreditType());

        if (customer.getCustomerType() == CustomerType.PERSONAL) {
            return validatePersonalCustomerRules(request, customer);
        } else {
            return validateBusinessCustomerRules(request, customer);
        }
    }

    /**
     * Validate rules for PERSONAL customers
     * - Can have only ONE personal loan
     * - Can have credit cards
     */
    private Mono<CreditRequest> validatePersonalCustomerRules(CreditRequest request, CustomerResponse customer) {
        if (request.getCreditType() == CreditType.PERSONAL_LOAN) {
            return creditRepository.countByCustomerIdAndCreditType(customer.getId(), CreditType.PERSONAL_LOAN)
                    .flatMap(count -> {
                        if (count > 0) {
                            return Mono.error(new BusinessRuleException(
                                    "Personal customer can only have one personal loan"));
                        }
                        return Mono.just(request);
                    });
        }

        // Credit cards are allowed for personal customers
        if (request.getCreditType() == CreditType.CREDIT_CARD) {
            return Mono.just(request);
        }

        // Business loans not allowed for personal customers
        if (request.getCreditType() == CreditType.BUSINESS_LOAN) {
            return Mono.error(new BusinessRuleException(
                    "Personal customers cannot have business loans"));
        }

        return Mono.just(request);
    }

    /**
     * Validate rules for BUSINESS customers
     * - Can have multiple business loans
     * - Can have credit cards
     * - Cannot have personal loans
     */
    private Mono<CreditRequest> validateBusinessCustomerRules(CreditRequest request, CustomerResponse customer) {
        if (request.getCreditType() == CreditType.PERSONAL_LOAN) {
            return Mono.error(new BusinessRuleException(
                    "Business customers cannot have personal loans"));
        }

        // Business loans and credit cards are allowed
        return Mono.just(request);
    }

    /**
     * Make a charge to a credit card
     * @param id credit id
     * @param chargeRequest charge request
     * @return Mono of CreditResponse
     */
    public Mono<CreditResponse> makeCharge(String id, ChargeRequest chargeRequest) {
        log.debug("Making charge of {} to credit: {}", chargeRequest.getAmount(), id);

        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(id)))
                .flatMap(credit -> {
                    if (!credit.isCreditCard()) {
                        return Mono.error(new BusinessRuleException(
                                "Charges can only be made to credit cards"));
                    }

                    if (!credit.getActive()) {
                        return Mono.error(new BusinessRuleException(
                                "Cannot charge to inactive credit"));
                    }

                    BigDecimal availableCredit = credit.getAvailableCredit();
                    if (chargeRequest.getAmount().compareTo(availableCredit) > 0) {
                        return Mono.error(new InsufficientCreditException(
                                chargeRequest.getAmount(), availableCredit));
                    }

                    // Add charge to balance
                    credit.setBalance(credit.getBalance().add(chargeRequest.getAmount()));
                    // Reduce credit limit
                    credit.setCreditLimit(credit.getCreditLimit().subtract(chargeRequest.getAmount()));
                    credit.setUpdatedAt(java.time.LocalDateTime.now());

                    return creditRepository.save(credit);
                })
                .doOnSuccess(credit -> log.info("Charge successful. New balance: {}", credit.getBalance()))
                .map(creditMapper::toResponse);
    }

    /**
     * Make a payment to a credit product
     * @param id credit id
     * @param paymentRequest payment request
     * @return Mono of CreditResponse
     */
    public Mono<CreditResponse> makePayment(String id, PaymentRequest paymentRequest) {
        log.debug("Making payment of {} to credit: {}", paymentRequest.getAmount(), id);

        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(id)))
                .flatMap(credit -> {
                    if (!credit.getActive()) {
                        return Mono.error(new BusinessRuleException(
                                "Cannot make payment to inactive credit"));
                    }

                    if (paymentRequest.getAmount().compareTo(credit.getBalance()) > 0) {
                        return Mono.error(new BusinessRuleException(
                                "Payment amount cannot exceed current balance"));
                    }

                    // Reduce balance
                    credit.setBalance(credit.getBalance().subtract(paymentRequest.getAmount()));

                    // If it's a credit card, restore credit limit
                    if (credit.isCreditCard()) {
                        credit.setCreditLimit(credit.getCreditLimit().add(paymentRequest.getAmount()));
                    }

                    credit.setUpdatedAt(java.time.LocalDateTime.now());

                    return creditRepository.save(credit);
                })
                .doOnSuccess(credit -> log.info("Payment successful. New balance: {}", credit.getBalance()))
                .map(creditMapper::toResponse);
    }

    /**
     * Find all credits
     * @return Flux of CreditResponse
     */
    public Flux<CreditResponse> findAll() {
        log.debug("Finding all credits");
        return creditRepository.findAll()
                .map(creditMapper::toResponse)
                .doOnComplete(() -> log.debug("Retrieved all credits"));
    }

    /**
     * Find credit by ID
     * @param id the credit id
     * @return Mono of CreditResponse
     */
    public Mono<CreditResponse> findById(String id) {
        log.debug("Finding credit by id: {}", id);
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(id)))
                .map(creditMapper::toResponse)
                .doOnSuccess(credit -> log.debug("Credit found with id: {}", id));
    }

    /**
     * Find credit by credit number
     * @param creditNumber the credit number
     * @return Mono of CreditResponse
     */
    public Mono<CreditResponse> findByCreditNumber(String creditNumber) {
        log.debug("Finding credit by credit number: {}", creditNumber);
        return creditRepository.findByCreditNumber(creditNumber)
                .switchIfEmpty(Mono.error(new CreditNotFoundException("creditNumber", creditNumber)))
                .map(creditMapper::toResponse)
                .doOnSuccess(credit -> log.debug("Credit found with number: {}", creditNumber));
    }

    /**
     * Find all credits by customer ID
     * @param customerId the customer id
     * @return Flux of CreditResponse
     */
    public Flux<CreditResponse> findByCustomerId(String customerId) {
        log.debug("Finding credits for customer: {}", customerId);
        return creditRepository.findByCustomerId(customerId)
                .map(creditMapper::toResponse)
                .doOnComplete(() -> log.debug("Retrieved credits for customer: {}", customerId));
    }

    /**
     * Update credit
     * @param id the credit id
     * @param request the credit request
     * @return Mono of CreditResponse
     */
    public Mono<CreditResponse> update(String id, CreditRequest request) {
        log.debug("Updating credit with id: {}", id);

        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(id)))
                .flatMap(existingCredit -> {
                    creditMapper.updateEntity(existingCredit, request);
                    return creditRepository.save(existingCredit);
                })
                .doOnSuccess(credit -> log.info("Credit updated successfully with id: {}", id))
                .map(creditMapper::toResponse);
    }

    /**
     * Delete credit by ID
     * @param id the credit id
     * @return Mono of Void
     */
    public Mono<Void> delete(String id) {
        log.debug("Deleting credit with id: {}", id);

        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(id)))
                .flatMap(credit -> creditRepository.deleteById(id)
                        .doOnSuccess(v -> log.info("Credit deleted successfully with id: {}", id)));
    }
}
