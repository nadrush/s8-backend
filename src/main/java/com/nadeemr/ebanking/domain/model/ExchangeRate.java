package com.nadeemr.ebanking.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Value object representing exchange rate for currency conversion
 */
public class ExchangeRate {
    
    private final String fromCurrency;
    private final String toCurrency;
    private final BigDecimal rate;
    private final LocalDate date;
    
    public ExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate, LocalDate date) {
        this.fromCurrency = Objects.requireNonNull(fromCurrency, "From currency cannot be null");
        this.toCurrency = Objects.requireNonNull(toCurrency, "To currency cannot be null");
        this.rate = Objects.requireNonNull(rate, "Rate cannot be null");
        this.date = Objects.requireNonNull(date, "Date cannot be null");
        
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }
    }
    
    public String getFromCurrency() {
        return fromCurrency;
    }
    
    public String getToCurrency() {
        return toCurrency;
    }
    
    public BigDecimal getRate() {
        return rate;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    /**
     * Convert amount using this exchange rate
     */
    public BigDecimal convert(BigDecimal amount) {
        return amount.multiply(rate);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return Objects.equals(fromCurrency, that.fromCurrency) &&
                Objects.equals(toCurrency, that.toCurrency) &&
                Objects.equals(rate, that.rate) &&
                Objects.equals(date, that.date);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, toCurrency, rate, date);
    }
    
    @Override
    public String toString() {
        return "ExchangeRate{" +
                "fromCurrency='" + fromCurrency + '\'' +
                ", toCurrency='" + toCurrency + '\'' +
                ", rate=" + rate +
                ", date=" + date +
                '}';
    }
}
