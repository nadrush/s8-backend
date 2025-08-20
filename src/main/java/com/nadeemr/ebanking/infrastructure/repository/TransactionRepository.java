package com.nadeemr.ebanking.infrastructure.repository;

import com.nadeemr.ebanking.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    /**
     * Find paginated transactions for a customer in a specific month
     */
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.valueDate >= :startDate AND t.valueDate <= :endDate " +
           "ORDER BY t.valueDate DESC, t.createdAt DESC")
    Page<Transaction> findByCustomerIdAndValueDateBetween(
            @Param("customerId") String customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    /**
     * Find paginated transactions for a customer in a specific month and account
     */
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.valueDate >= :startDate AND t.valueDate <= :endDate " +
           "AND t.accountIban = :accountIban " +
           "ORDER BY t.valueDate DESC, t.createdAt DESC")
    Page<Transaction> findByCustomerIdAndValueDateBetweenAndAccountIban(
            @Param("customerId") String customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("accountIban") String accountIban,
            Pageable pageable);
    
    /**
     * Find all transactions for a customer in a specific month (for summary calculation)
     */
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.valueDate >= :startDate AND t.valueDate <= :endDate")
    List<Transaction> findAllByCustomerIdAndValueDateBetween(
            @Param("customerId") String customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Find all transactions for a customer in a specific month and account (for summary calculation)
     */
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.valueDate >= :startDate AND t.valueDate <= :endDate " +
           "AND t.accountIban = :accountIban")
    List<Transaction> findAllByCustomerIdAndValueDateBetweenAndAccountIban(
            @Param("customerId") String customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("accountIban") String accountIban);
    
    /**
     * Check if a transaction exists by ID
     */
    boolean existsById(String id);
    
    /**
     * Count transactions for a customer
     */
    long countByCustomerId(String customerId);
}
