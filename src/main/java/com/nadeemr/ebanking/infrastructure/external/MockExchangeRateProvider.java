package com.nadeemr.ebanking.infrastructure.external;

import com.nadeemr.ebanking.domain.model.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mock implementation of ExchangeRateProvider for demonstration purposes
 * In a real implementation, this would integrate with an actual exchange rate API
 */
@Service
public class MockExchangeRateProvider implements ExchangeRateProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(MockExchangeRateProvider.class);
    
    private final RestTemplate restTemplate;
    private final Set<String> supportedCurrencies = Set.of("EUR", "USD", "GBP", "CHF", "JPY");
    
    // Mock exchange rates (in a real implementation, these would come from external API)
    private final Map<String, BigDecimal> mockRates = Map.of(
        "EUR_USD", new BigDecimal("1.0950"),
        "EUR_GBP", new BigDecimal("0.8750"),
        "EUR_CHF", new BigDecimal("0.9850"),
        "EUR_JPY", new BigDecimal("145.50"),
        "USD_EUR", new BigDecimal("0.9132"),
        "GBP_EUR", new BigDecimal("1.1429"),
        "CHF_EUR", new BigDecimal("1.0152"),
        "JPY_EUR", new BigDecimal("0.00687")
    );
    
    public MockExchangeRateProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    @Cacheable(value = "exchangeRates", key = "#fromCurrency + '_' + #toCurrency + '_' + #date")
    public Optional<ExchangeRate> getExchangeRate(String fromCurrency, String toCurrency, LocalDate date) {
        logger.debug("Fetching exchange rate from {} to {} for date {}", fromCurrency, toCurrency, date);
        
        // Return 1.0 for same currency
        if (fromCurrency.equals(toCurrency)) {
            return Optional.of(new ExchangeRate(fromCurrency, toCurrency, BigDecimal.ONE, date));
        }
        
        // Check if currencies are supported
        if (!supportsCurrency(fromCurrency) || !supportsCurrency(toCurrency)) {
            logger.warn("Unsupported currency pair: {} to {}", fromCurrency, toCurrency);
            return Optional.empty();
        }
        
        try {
            // In a real implementation, this would call an external API
            BigDecimal rate = getMockExchangeRate(fromCurrency, toCurrency);
            
            if (rate != null) {
                ExchangeRate exchangeRate = new ExchangeRate(fromCurrency, toCurrency, rate, date);
                logger.debug("Found exchange rate: {}", exchangeRate);
                return Optional.of(exchangeRate);
            }
            
            logger.warn("No exchange rate found for {} to {}", fromCurrency, toCurrency);
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error fetching exchange rate from {} to {}: {}", fromCurrency, toCurrency, e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public List<ExchangeRate> getExchangeRates(List<String> fromCurrencies, String toCurrency, LocalDate date) {
        logger.debug("Fetching exchange rates for currencies {} to {} for date {}", fromCurrencies, toCurrency, date);
        
        return fromCurrencies.stream()
                .map(fromCurrency -> getExchangeRate(fromCurrency, toCurrency, date))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean supportsCurrency(String currency) {
        return supportedCurrencies.contains(currency);
    }
    
    /**
     * Mock method to simulate external API call
     * In a real implementation, this would be replaced with actual API integration
     */
    private BigDecimal getMockExchangeRate(String fromCurrency, String toCurrency) {
        String key = fromCurrency + "_" + toCurrency;
        BigDecimal rate = mockRates.get(key);
        
        if (rate == null) {
            // Try reverse rate
            String reverseKey = toCurrency + "_" + fromCurrency;
            BigDecimal reverseRate = mockRates.get(reverseKey);
            if (reverseRate != null) {
                rate = BigDecimal.ONE.divide(reverseRate, 6, BigDecimal.ROUND_HALF_UP);
            }
        }
        
        return rate;
    }
    
    /**
     * Method for real external API integration (commented for mock implementation)
     */
    private ExchangeRate callExternalAPI(String fromCurrency, String toCurrency, LocalDate date) {
        try {
            String url = String.format("https://api.exchangerate.com/v1/%s?from=%s&to=%s&date=%s",
                    "latest", fromCurrency, toCurrency, date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            // This would be the actual API call
            // ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);
            // return mapToExchangeRate(response);
            
            return null; // Placeholder
        } catch (RestClientException e) {
            logger.error("Failed to call external exchange rate API: {}", e.getMessage());
            throw new RuntimeException("Exchange rate service unavailable", e);
        }
    }
}
