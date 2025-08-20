package com.nadeemr.ebanking.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "Transaction data transfer object")
public class TransactionDto {
    
    @Schema(description = "Unique transaction identifier", example = "89d3o179-abcd-465b-o9ee-e2d5f6ofEld46")
    @NotNull
    @Size(min = 36, max = 36)
    private String id;
    
    @Schema(description = "Transaction amount", example = "100.50")
    @NotNull
    private BigDecimal amount;
    
    @Schema(description = "Currency code", example = "GBP")
    @NotNull
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
    private String currency;
    
    @Schema(description = "Account IBAN", example = "CH93-0000-0000-0000-0000-0")
    @NotNull
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$")
    private String accountIban;
    
    @Schema(description = "Value date", example = "2020-10-01")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate valueDate;
    
    @Schema(description = "Transaction description", example = "Online payment CHF")
    @NotNull
    @Size(max = 500)
    private String description;
    
    @Schema(description = "Amount converted to base currency", example = "85.75")
    private BigDecimal convertedAmount;
    
    @Schema(description = "Base currency for conversion", example = "EUR")
    private String baseCurrency;
    
    public TransactionDto() {}
    
    public TransactionDto(String id, BigDecimal amount, String currency, String accountIban, 
                         LocalDate valueDate, String description) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.accountIban = accountIban;
        this.valueDate = valueDate;
        this.description = description;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getAccountIban() {
        return accountIban;
    }
    
    public void setAccountIban(String accountIban) {
        this.accountIban = accountIban;
    }
    
    public LocalDate getValueDate() {
        return valueDate;
    }
    
    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }
    
    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }
    
    public String getBaseCurrency() {
        return baseCurrency;
    }
    
    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionDto that = (TransactionDto) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TransactionDto{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", accountIban='" + accountIban + '\'' +
                ", valueDate=" + valueDate +
                ", description='" + description + '\'' +
                '}';
    }
}
