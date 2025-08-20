package com.nadeemr.ebanking.infrastructure.external;

import com.nadeemr.ebanking.domain.model.ExchangeRate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface for external exchange rate provider
 */
public interface ExchangeRateProvider {
    
    /**
     * Get exchange rate for a specific date and currency pair
     */
    Optional<ExchangeRate> getExchangeRate(String fromCurrency, String toCurrency, LocalDate date);
    
    /**
     * Get multiple exchange rates for a list of currencies to a base currency
     */
    List<ExchangeRate> getExchangeRates(List<String> fromCurrencies, String toCurrency, LocalDate date);
    
    /**
     * Check if the provider supports a specific currency
     */
    boolean supportsCurrency(String currency);
}
