package com.nadeemr.ebanking.domain.service;

import com.nadeemr.ebanking.api.dto.TransactionDto;
import com.nadeemr.ebanking.api.dto.TransactionPageDto;
import com.nadeemr.ebanking.api.dto.TransactionSearchDto;
import com.nadeemr.ebanking.domain.model.ExchangeRate;
import com.nadeemr.ebanking.domain.model.Money;
import com.nadeemr.ebanking.domain.model.Transaction;
import com.nadeemr.ebanking.infrastructure.external.ExchangeRateProvider;
import com.nadeemr.ebanking.infrastructure.repository.TransactionRepository;
import com.nadeemr.ebanking.util.TransactionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    
    private final TransactionRepository transactionRepository;
    private final ExchangeRateProvider exchangeRateProvider;
    private final TransactionMapper transactionMapper;
    
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                 ExchangeRateProvider exchangeRateProvider,
                                 TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.exchangeRateProvider = exchangeRateProvider;
        this.transactionMapper = transactionMapper;
    }
    
    @Override
    public TransactionPageDto getTransactions(String customerId, TransactionSearchDto searchCriteria) {
        logger.debug("Getting transactions for customer {} with criteria: {}", customerId, searchCriteria);
        
        // Parse year-month
        YearMonth yearMonth = YearMonth.parse(searchCriteria.getYearMonth(), DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // Create pageable
        Pageable pageable = PageRequest.of(searchCriteria.getPage(), searchCriteria.getSize());
        
        // Fetch paginated transactions
        Page<Transaction> transactionPage;
        List<Transaction> allTransactions;
        
        if (searchCriteria.getAccountIban() != null && !searchCriteria.getAccountIban().trim().isEmpty()) {
            transactionPage = transactionRepository.findByCustomerIdAndValueDateBetweenAndAccountIban(
                    customerId, startDate, endDate, searchCriteria.getAccountIban(), pageable);
            allTransactions = transactionRepository.findAllByCustomerIdAndValueDateBetweenAndAccountIban(
                    customerId, startDate, endDate, searchCriteria.getAccountIban());
        } else {
            transactionPage = transactionRepository.findByCustomerIdAndValueDateBetween(
                    customerId, startDate, endDate, pageable);
            allTransactions = transactionRepository.findAllByCustomerIdAndValueDateBetween(
                    customerId, startDate, endDate);
        }
        
        // Convert transactions to DTOs with currency conversion
        List<TransactionDto> transactionDtos = convertTransactionsWithExchangeRates(
                transactionPage.getContent(), searchCriteria.getBaseCurrency());
        
        // Calculate summary for all transactions in the month
        TransactionPageDto.TransactionSummary summary = calculateSummary(
                allTransactions, searchCriteria.getBaseCurrency());
        
        // Create page info
        TransactionPageDto.PageInfo pageInfo = new TransactionPageDto.PageInfo(
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isFirst(),
                transactionPage.isLast()
        );
        
        logger.debug("Found {} transactions for customer {} in month {}", 
                    transactionPage.getTotalElements(), customerId, searchCriteria.getYearMonth());
        
        return new TransactionPageDto(transactionDtos, pageInfo, summary);
    }
    
    private List<TransactionDto> convertTransactionsWithExchangeRates(List<Transaction> transactions, String baseCurrency) {
        // Get unique currencies and current date
        List<String> currencies = transactions.stream()
                .map(Transaction::getCurrency)
                .distinct()
                .collect(Collectors.toList());
        
        LocalDate currentDate = LocalDate.now();
        
        // Fetch exchange rates for all currencies
        Map<String, ExchangeRate> exchangeRateMap = exchangeRateProvider
                .getExchangeRates(currencies, baseCurrency, currentDate)
                .stream()
                .collect(Collectors.toMap(
                        rate -> rate.getFromCurrency() + "_" + rate.getToCurrency(),
                        rate -> rate
                ));
        
        // Convert transactions with exchange rates
        return transactions.stream()
                .map(transaction -> {
                    TransactionDto dto = transactionMapper.toDto(transaction);
                    dto.setBaseCurrency(baseCurrency);
                    
                    // Convert amount if different currency
                    if (!transaction.getCurrency().equals(baseCurrency)) {
                        String rateKey = transaction.getCurrency() + "_" + baseCurrency;
                        ExchangeRate exchangeRate = exchangeRateMap.get(rateKey);
                        
                        if (exchangeRate != null) {
                            Money originalMoney = new Money(transaction.getAmount(), transaction.getCurrency());
                            Money convertedMoney = originalMoney.convertTo(baseCurrency, exchangeRate);
                            dto.setConvertedAmount(convertedMoney.getAmount());
                        } else {
                            logger.warn("No exchange rate found for {} to {}", transaction.getCurrency(), baseCurrency);
                            dto.setConvertedAmount(transaction.getAmount()); // Fallback to original amount
                        }
                    } else {
                        dto.setConvertedAmount(transaction.getAmount());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    private TransactionPageDto.TransactionSummary calculateSummary(List<Transaction> transactions, String baseCurrency) {
        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;
        
        LocalDate currentDate = LocalDate.now();
        
        // Get unique currencies for exchange rate lookup
        List<String> currencies = transactions.stream()
                .map(Transaction::getCurrency)
                .distinct()
                .collect(Collectors.toList());
        
        Map<String, ExchangeRate> exchangeRateMap = exchangeRateProvider
                .getExchangeRates(currencies, baseCurrency, currentDate)
                .stream()
                .collect(Collectors.toMap(
                        rate -> rate.getFromCurrency() + "_" + rate.getToCurrency(),
                        rate -> rate
                ));
        
        for (Transaction transaction : transactions) {
            BigDecimal convertedAmount = transaction.getAmount();
            
            // Convert amount if different currency
            if (!transaction.getCurrency().equals(baseCurrency)) {
                String rateKey = transaction.getCurrency() + "_" + baseCurrency;
                ExchangeRate exchangeRate = exchangeRateMap.get(rateKey);
                
                if (exchangeRate != null) {
                    Money originalMoney = new Money(transaction.getAmount(), transaction.getCurrency());
                    Money convertedMoney = originalMoney.convertTo(baseCurrency, exchangeRate);
                    convertedAmount = convertedMoney.getAmount();
                }
            }
            
            // Add to credit or debit
            if (convertedAmount.compareTo(BigDecimal.ZERO) >= 0) {
                totalCredit = totalCredit.add(convertedAmount);
            } else {
                totalDebit = totalDebit.add(convertedAmount.abs());
            }
        }
        
        BigDecimal netAmount = totalCredit.subtract(totalDebit);
        
        return new TransactionPageDto.TransactionSummary(totalCredit, totalDebit, netAmount, baseCurrency);
    }
}
