package com.nadeemr.ebanking.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Kafka message representing a transaction event
 */
public class TransactionEvent {
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("accountIban")
    private String accountIban;
    
    @JsonProperty("valueDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate valueDate;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("eventType")
    private String eventType; // CREATE, UPDATE, DELETE
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    public TransactionEvent() {}
    
    public TransactionEvent(String transactionId, BigDecimal amount, String currency, 
                           String accountIban, LocalDate valueDate, String description, 
                           String customerId, String eventType) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.accountIban = accountIban;
        this.valueDate = valueDate;
        this.description = description;
        this.customerId = customerId;
        this.eventType = eventType;
    }
    
    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionEvent that = (TransactionEvent) o;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    @Override
    public String toString() {
        return "TransactionEvent{" +
                "transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", accountIban='" + accountIban + '\'' +
                ", valueDate=" + valueDate +
                ", description='" + description + '\'' +
                ", customerId='" + customerId + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
