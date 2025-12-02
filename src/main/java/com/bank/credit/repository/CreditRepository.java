package com.bank.credit.repository;

import com.bank.credit.model.entity.Credit;
import com.bank.credit.model.enums.CreditType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for Credit entity
 * Provides CRUD operations and custom queries
 */
@Repository
public interface CreditRepository extends ReactiveMongoRepository<Credit, String> {

    /**
     * Find credit by credit number
     * @param creditNumber the credit number
     * @return Mono of Credit
     */
    Mono<Credit> findByCreditNumber(String creditNumber);

    /**
     * Find all credits by customer ID
     * @param customerId the customer id
     * @return Flux of Credits
     */
    Flux<Credit> findByCustomerId(String customerId);

    /**
     * Find credits by customer ID and credit type
     * @param customerId the customer id
     * @param creditType the credit type
     * @return Flux of Credits
     */
    Flux<Credit> findByCustomerIdAndCreditType(String customerId, CreditType creditType);

    /**
     * Check if credit exists by credit number
     * @param creditNumber the credit number
     * @return Mono of Boolean
     */
    Mono<Boolean> existsByCreditNumber(String creditNumber);

    /**
     * Count credits by customer ID and credit type
     * @param customerId the customer id
     * @param creditType the credit type
     * @return Mono of Long
     */
    Mono<Long> countByCustomerIdAndCreditType(String customerId, CreditType creditType);

    /**
     * Find active credits by customer ID
     * @param customerId the customer id
     * @param active active status
     * @return Flux of Credits
     */
    Flux<Credit> findByCustomerIdAndActive(String customerId, Boolean active);
}
