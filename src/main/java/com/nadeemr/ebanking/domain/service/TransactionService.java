package com.nadeemr.ebanking.domain.service;

import com.nadeemr.ebanking.api.dto.TransactionPageDto;
import com.nadeemr.ebanking.api.dto.TransactionSearchDto;

/**
 * Service interface for transaction operations
 */
public interface TransactionService {
    
    /**
     * Get paginated transactions for a customer in a specific month
     * 
     * @param customerId the customer identifier
     * @param searchCriteria the search criteria including pagination and filters
     * @return paginated transaction response with totals
     */
    TransactionPageDto getTransactions(String customerId, TransactionSearchDto searchCriteria);
}
