package com.nadeemr.ebanking.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value object representing a monetary amount with currency
 */
public class Money {
    
    private final BigDecimal amount;
    private final String currency;
    
    public Money(BigDecimal amount, String currency) {
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
        
        if (currency.length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-letter code");
        }
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    /**
     * Convert this money to another currency using the provided exchange rate
     */
    public Money convertTo(String targetCurrency, ExchangeRate exchangeRate) {
        if (!this.currency.equals(exchangeRate.getFromCurrency()) || 
            !targetCurrency.equals(exchangeRate.getToCurrency())) {
            throw new IllegalArgumentException("Exchange rate does not match currencies");
        }
        
        BigDecimal convertedAmount = exchangeRate.convert(this.amount);
        return new Money(convertedAmount, targetCurrency);
    }
    
    /**
     * Add another money amount (must be same currency)
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtract another money amount (must be same currency)
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) &&
                Objects.equals(currency, money.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return currency + " " + amount;
    }
}
