package com.bank.credit.controller;

import com.bank.credit.model.dto.ChargeRequest;
import com.bank.credit.model.dto.CreditRequest;
import com.bank.credit.model.dto.CreditResponse;
import com.bank.credit.model.dto.PaymentRequest;
import com.bank.credit.service.CreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller for Credit operations
 * Provides endpoints for CRUD operations and credit transactions
 */
@Slf4j
@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    /**
     * Create a new credit
     * POST /api/credits
     * @param request the credit request
     * @return Mono of CreditResponse with 201 status
     */
    @PostMapping
    public Mono<ResponseEntity<CreditResponse>> create(@Valid @RequestBody CreditRequest request) {
        log.info("POST /api/credits - Creating credit for customer: {}", request.getCustomerId());
        return creditService.create(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    /**
     * Make a charge to a credit card
     * POST /api/credits/{id}/charge
     * @param id credit id
     * @param chargeRequest charge request
     * @return Mono of CreditResponse with 200 status
     */
    @PostMapping("/{id}/charge")
    public Mono<ResponseEntity<CreditResponse>> makeCharge(
            @PathVariable String id,
            @Valid @RequestBody ChargeRequest chargeRequest) {
        log.info("POST /api/credits/{}/charge - Making charge of {}", id, chargeRequest.getAmount());
        return creditService.makeCharge(id, chargeRequest)
                .map(ResponseEntity::ok);
    }

    /**
     * Make a payment to a credit
     * POST /api/credits/{id}/payment
     * @param id credit id
     * @param paymentRequest payment request
     * @return Mono of CreditResponse with 200 status
     */
    @PostMapping("/{id}/payment")
    public Mono<ResponseEntity<CreditResponse>> makePayment(
            @PathVariable String id,
            @Valid @RequestBody PaymentRequest paymentRequest) {
        log.info("POST /api/credits/{}/payment - Making payment of {}", id, paymentRequest.getAmount());
        return creditService.makePayment(id, paymentRequest)
                .map(ResponseEntity::ok);
    }

    /**
     * Get all credits
     * GET /api/credits
     * @return Flux of CreditResponse with 200 status
     */
    @GetMapping
    public Mono<ResponseEntity<Flux<CreditResponse>>> findAll() {
        log.info("GET /api/credits - Fetching all credits");
        return Mono.just(ResponseEntity.ok(creditService.findAll()));
    }

    /**
     * Get credit by ID
     * GET /api/credits/{id}
     * @param id the credit id
     * @return Mono of CreditResponse with 200 status
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CreditResponse>> findById(@PathVariable String id) {
        log.info("GET /api/credits/{} - Fetching credit by id", id);
        return creditService.findById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Get credit by credit number
     * GET /api/credits/number/{creditNumber}
     * @param creditNumber the credit number
     * @return Mono of CreditResponse with 200 status
     */
    @GetMapping("/number/{creditNumber}")
    public Mono<ResponseEntity<CreditResponse>> findByCreditNumber(@PathVariable String creditNumber) {
        log.info("GET /api/credits/number/{} - Fetching credit by number", creditNumber);
        return creditService.findByCreditNumber(creditNumber)
                .map(ResponseEntity::ok);
    }

    /**
     * Get all credits by customer ID
     * GET /api/credits/customer/{customerId}
     * @param customerId the customer id
     * @return Flux of CreditResponse with 200 status
     */
    @GetMapping("/customer/{customerId}")
    public Mono<ResponseEntity<Flux<CreditResponse>>> findByCustomerId(@PathVariable String customerId) {
        log.info("GET /api/credits/customer/{} - Fetching credits for customer", customerId);
        return Mono.just(ResponseEntity.ok(creditService.findByCustomerId(customerId)));
    }

    /**
     * Update credit
     * PUT /api/credits/{id}
     * @param id the credit id
     * @param request the credit request
     * @return Mono of CreditResponse with 200 status
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CreditResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreditRequest request) {
        log.info("PUT /api/credits/{} - Updating credit", id);
        return creditService.update(id, request)
                .map(ResponseEntity::ok);
    }

    /**
     * Delete credit
     * DELETE /api/credits/{id}
     * @param id the credit id
     * @return Mono of Void with 204 status
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        log.info("DELETE /api/credits/{} - Deleting credit", id);
        return creditService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
