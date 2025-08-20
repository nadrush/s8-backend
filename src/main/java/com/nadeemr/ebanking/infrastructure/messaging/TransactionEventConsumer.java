package com.nadeemr.ebanking.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadeemr.ebanking.domain.model.Transaction;
import com.nadeemr.ebanking.infrastructure.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka consumer for transaction events
 */
@Service
public class TransactionEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionEventConsumer.class);
    
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    
    public TransactionEventConsumer(TransactionRepository transactionRepository, ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }
    
    @KafkaListener(topics = "${app.kafka.transaction-topic}", groupId = "${app.kafka.consumer-group}")
    @Transactional
    public void consumeTransactionEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        logger.info("Received transaction event - Topic: {}, Partition: {}, Offset: {}, Key: {}", 
                   topic, partition, offset, key);
        
        try {
            TransactionEvent event = objectMapper.readValue(payload, TransactionEvent.class);
            logger.debug("Parsed transaction event: {}", event);
            
            processTransactionEvent(event);
            
            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();
            logger.debug("Successfully processed and acknowledged transaction event with key: {}", key);
            
        } catch (Exception e) {
            logger.error("Error processing transaction event with key {}: {}", key, e.getMessage(), e);
            // Don't acknowledge - this will cause the message to be retried
            // In production, you might want to implement dead letter topic handling
        }
    }
    
    private void processTransactionEvent(TransactionEvent event) {
        String eventType = event.getEventType();
        String transactionId = event.getTransactionId();
        
        logger.debug("Processing {} event for transaction {}", eventType, transactionId);
        
        switch (eventType.toUpperCase()) {
            case "CREATE":
            case "UPDATE":
                saveOrUpdateTransaction(event);
                break;
            case "DELETE":
                deleteTransaction(transactionId);
                break;
            default:
                logger.warn("Unknown event type: {} for transaction {}", eventType, transactionId);
        }
    }
    
    private void saveOrUpdateTransaction(TransactionEvent event) {
        try {
            Transaction transaction = mapToTransaction(event);
            
            // Check if transaction already exists
            boolean exists = transactionRepository.existsById(transaction.getId());
            
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            String action = exists ? "Updated" : "Created";
            logger.info("{} transaction: {}", action, savedTransaction.getId());
            
        } catch (Exception e) {
            logger.error("Error saving/updating transaction {}: {}", event.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save/update transaction", e);
        }
    }
    
    private void deleteTransaction(String transactionId) {
        try {
            if (transactionRepository.existsById(transactionId)) {
                transactionRepository.deleteById(transactionId);
                logger.info("Deleted transaction: {}", transactionId);
            } else {
                logger.warn("Attempted to delete non-existent transaction: {}", transactionId);
            }
        } catch (Exception e) {
            logger.error("Error deleting transaction {}: {}", transactionId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }
    
    private Transaction mapToTransaction(TransactionEvent event) {
        Transaction transaction = new Transaction();
        transaction.setId(event.getTransactionId());
        transaction.setAmount(event.getAmount());
        transaction.setCurrency(event.getCurrency());
        transaction.setAccountIban(event.getAccountIban());
        transaction.setValueDate(event.getValueDate());
        transaction.setDescription(event.getDescription());
        transaction.setCustomerId(event.getCustomerId());
        
        return transaction;
    }
}
