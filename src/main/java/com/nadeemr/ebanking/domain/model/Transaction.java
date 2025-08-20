package com.nadeemr.ebanking.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_customer_id", columnList = "customerId"),
    @Index(name = "idx_account_iban", columnList = "accountIban"),
    @Index(name = "idx_value_date", columnList = "valueDate"),
    @Index(name = "idx_customer_value_date", columnList = "customerId, valueDate")
})
public class Transaction {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    
    @NotNull
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
    @Column(nullable = false, length = 3)
    private String currency;
    
    @NotNull
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$", 
             message = "Invalid IBAN format")
    @Column(nullable = false, length = 34)
    private String accountIban;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate valueDate;
    
    @NotNull
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String description;
    
    @NotNull
    @Pattern(regexp = "^P-[0-9]{10}$", message = "Invalid customer ID format")
    @Column(nullable = false, length = 12)
    private String customerId;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public Transaction() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    public Transaction(String id, BigDecimal amount, String currency, String accountIban, 
                      LocalDate valueDate, String description, String customerId) {
        this();
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.accountIban = accountIban;
        this.valueDate = valueDate;
        this.description = description;
        this.customerId = customerId;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", accountIban='" + accountIban + '\'' +
                ", valueDate=" + valueDate +
                ", description='" + description + '\'' +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}
