package com.nadeemr.ebanking.domain.service;

import com.nadeemr.ebanking.api.dto.TransactionPageDto;
import com.nadeemr.ebanking.api.dto.TransactionSearchDto;
import com.nadeemr.ebanking.domain.model.ExchangeRate;
import com.nadeemr.ebanking.domain.model.Transaction;
import com.nadeemr.ebanking.infrastructure.external.ExchangeRateProvider;
import com.nadeemr.ebanking.infrastructure.repository.TransactionRepository;
import com.nadeemr.ebanking.util.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private ExchangeRateProvider exchangeRateProvider;
    
    @Mock
    private TransactionMapper transactionMapper;
    
    @InjectMocks
    private TransactionServiceImpl transactionService;
    
    private Transaction testTransaction1;
    private Transaction testTransaction2;
    private String customerId;
    
    @BeforeEach
    void setUp() {
        customerId = "P-0123456789";
        
        testTransaction1 = new Transaction(
            "89d3o179-abcd-465b-o9ee-e2d5f6ofEld46",
            new BigDecimal("100.50"),
            "GBP",
            "GB82WEST12345698765432",
            LocalDate.of(2023, 10, 1),
            "Online payment GBP",
            customerId
        );
        
        testTransaction2 = new Transaction(
            "89d3o179-abcd-465b-o9ee-e2d5f6ofEld47",
            new BigDecimal("-75.25"),
            "USD",
            "US64SVBKUS6S3300958879",
            LocalDate.of(2023, 10, 2),
            "ATM withdrawal USD",
            customerId
        );
    }
    
    @Test
    void getTransactions_ShouldReturnPagedResults() {
        // Given
        TransactionSearchDto searchCriteria = new TransactionSearchDto("2023-10", 0, 10, "EUR");
        
        List<Transaction> transactions = Arrays.asList(testTransaction1, testTransaction2);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10), 2);
        
        ExchangeRate gbpToEur = new ExchangeRate("GBP", "EUR", new BigDecimal("1.1429"), LocalDate.now());
        ExchangeRate usdToEur = new ExchangeRate("USD", "EUR", new BigDecimal("0.9132"), LocalDate.now());
        
        when(transactionRepository.findByCustomerIdAndValueDateBetween(
            eq(customerId), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
            .thenReturn(transactionPage);
        
        when(transactionRepository.findAllByCustomerIdAndValueDateBetween(
            eq(customerId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(transactions);
        
        when(exchangeRateProvider.getExchangeRates(anyList(), eq("EUR"), any(LocalDate.class)))
            .thenReturn(Arrays.asList(gbpToEur, usdToEur));
        
        when(transactionMapper.toDto(any(Transaction.class)))
            .thenReturn(createMockTransactionDto(testTransaction1))
            .thenReturn(createMockTransactionDto(testTransaction2));
        
        // When
        TransactionPageDto result = transactionService.getTransactions(customerId, searchCriteria);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getTransactions().size());
        assertEquals(0, result.getPageInfo().getPage());
        assertEquals(10, result.getPageInfo().getSize());
        assertEquals(2, result.getPageInfo().getTotalElements());
        assertEquals(1, result.getPageInfo().getTotalPages());
        assertTrue(result.getPageInfo().isFirst());
        assertTrue(result.getPageInfo().isLast());
        
        assertNotNull(result.getSummary());
        assertEquals("EUR", result.getSummary().getBaseCurrency());
        
        verify(transactionRepository).findByCustomerIdAndValueDateBetween(
            eq(customerId), any(LocalDate.class), any(LocalDate.class), any(Pageable.class));
        verify(exchangeRateProvider, times(2)).getExchangeRates(anyList(), eq("EUR"), any(LocalDate.class));
    }
    
    @Test
    void getTransactions_WithAccountFilter_ShouldCallCorrectRepositoryMethod() {
        // Given
        TransactionSearchDto searchCriteria = new TransactionSearchDto("2023-10", 0, 10, "EUR");
        searchCriteria.setAccountIban("GB82WEST12345698765432");
        
        List<Transaction> transactions = Collections.singletonList(testTransaction1);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10), 1);
        
        when(transactionRepository.findByCustomerIdAndValueDateBetweenAndAccountIban(
            eq(customerId), any(LocalDate.class), any(LocalDate.class), eq("GB82WEST12345698765432"), any(Pageable.class)))
            .thenReturn(transactionPage);
        
        when(transactionRepository.findAllByCustomerIdAndValueDateBetweenAndAccountIban(
            eq(customerId), any(LocalDate.class), any(LocalDate.class), eq("GB82WEST12345698765432")))
            .thenReturn(transactions);
        
        when(exchangeRateProvider.getExchangeRates(anyList(), eq("EUR"), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        
        when(transactionMapper.toDto(any(Transaction.class)))
            .thenReturn(createMockTransactionDto(testTransaction1));
        
        // When
        TransactionPageDto result = transactionService.getTransactions(customerId, searchCriteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTransactions().size());
        
        verify(transactionRepository).findByCustomerIdAndValueDateBetweenAndAccountIban(
            eq(customerId), any(LocalDate.class), any(LocalDate.class), eq("GB82WEST12345698765432"), any(Pageable.class));
    }
    
    @Test
    void getTransactions_EmptyResult_ShouldReturnEmptyPage() {
        // Given
        TransactionSearchDto searchCriteria = new TransactionSearchDto("2023-10", 0, 10, "EUR");
        
        Page<Transaction> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        
        when(transactionRepository.findByCustomerIdAndValueDateBetween(
            eq(customerId), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
            .thenReturn(emptyPage);
        
        when(transactionRepository.findAllByCustomerIdAndValueDateBetween(
            eq(customerId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        
        when(exchangeRateProvider.getExchangeRates(anyList(), eq("EUR"), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());
        
        // When
        TransactionPageDto result = transactionService.getTransactions(customerId, searchCriteria);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getTransactions().size());
        assertEquals(0, result.getPageInfo().getTotalElements());
        assertEquals(BigDecimal.ZERO, result.getSummary().getTotalCredit());
        assertEquals(BigDecimal.ZERO, result.getSummary().getTotalDebit());
        assertEquals(BigDecimal.ZERO, result.getSummary().getNetAmount());
    }
    
    private com.nadeemr.ebanking.api.dto.TransactionDto createMockTransactionDto(Transaction transaction) {
        com.nadeemr.ebanking.api.dto.TransactionDto dto = new com.nadeemr.ebanking.api.dto.TransactionDto();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setAccountIban(transaction.getAccountIban());
        dto.setValueDate(transaction.getValueDate());
        dto.setDescription(transaction.getDescription());
        return dto;
    }
}
