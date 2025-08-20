package com.nadeemr.ebanking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

@Schema(description = "Transaction search criteria")
public class TransactionSearchDto {
    
    @Schema(description = "Year and month in YYYY-MM format", example = "2023-10", required = true)
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Date must be in YYYY-MM format")
    private String yearMonth;
    
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "Page number must be non-negative")
    private int page = 0;
    
    @Schema(description = "Page size", example = "20", defaultValue = "20")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private int size = 20;
    
    @Schema(description = "Base currency for amount conversion", example = "EUR")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
    private String baseCurrency = "EUR";
    
    @Schema(description = "Account IBAN filter (optional)", example = "CH93-0000-0000-0000-0000-0")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$")
    private String accountIban;
    
    public TransactionSearchDto() {}
    
    public TransactionSearchDto(String yearMonth, int page, int size, String baseCurrency) {
        this.yearMonth = yearMonth;
        this.page = page;
        this.size = size;
        this.baseCurrency = baseCurrency;
    }
    
    // Getters and Setters
    public String getYearMonth() {
        return yearMonth;
    }
    
    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public String getBaseCurrency() {
        return baseCurrency;
    }
    
    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }
    
    public String getAccountIban() {
        return accountIban;
    }
    
    public void setAccountIban(String accountIban) {
        this.accountIban = accountIban;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionSearchDto that = (TransactionSearchDto) o;
        return page == that.page &&
                size == that.size &&
                Objects.equals(yearMonth, that.yearMonth) &&
                Objects.equals(baseCurrency, that.baseCurrency) &&
                Objects.equals(accountIban, that.accountIban);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(yearMonth, page, size, baseCurrency, accountIban);
    }
    
    @Override
    public String toString() {
        return "TransactionSearchDto{" +
                "yearMonth='" + yearMonth + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", baseCurrency='" + baseCurrency + '\'' +
                ", accountIban='" + accountIban + '\'' +
                '}';
    }
}
