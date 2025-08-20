package com.nadeemr.ebanking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadeemr.ebanking.api.dto.TransactionPageDto;
import com.nadeemr.ebanking.api.dto.TransactionSearchDto;
import com.nadeemr.ebanking.domain.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
    "spring.main.banner-mode=off",
    "logging.level.com.nadeemr.ebanking=WARN"
})
class TransactionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TransactionService transactionService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private TransactionPageDto mockResponse;
    
    @BeforeEach
    void setUp() {
        TransactionPageDto.PageInfo pageInfo = new TransactionPageDto.PageInfo(0, 10, 0, 0, true, true);
        TransactionPageDto.TransactionSummary summary = new TransactionPageDto.TransactionSummary(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "EUR");
        mockResponse = new TransactionPageDto(Collections.emptyList(), pageInfo, summary);
    }
    
    @Test
    @WithMockUser(username = "P-0123456789", roles = "CUSTOMER")
    void getTransactions_ValidRequest_ShouldReturnOk() throws Exception {
        // Given
        when(transactionService.getTransactions(eq("P-0123456789"), any(TransactionSearchDto.class)))
            .thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(get("/api/v1/transactions")
                .param("yearMonth", "2023-10")
                .param("page", "0")
                .param("size", "10")
                .param("baseCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pageInfo.page").value(0))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.summary.baseCurrency").value("EUR"));
    }
    
    @Test
    @WithMockUser(username = "P-0123456789", roles = "CUSTOMER")
    void getTransactions_InvalidYearMonth_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                .param("yearMonth", "invalid-date")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "P-0123456789", roles = "CUSTOMER")
    void getTransactions_InvalidCurrency_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                .param("yearMonth", "2023-10")
                .param("baseCurrency", "INVALID"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getTransactions_NoAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                .param("yearMonth", "2023-10"))
                .andExpect(status().isUnauthorized());
    }
    
        @Test
    @WithMockUser(username = "P-0123456789", roles = "CUSTOMER")
    void searchTransactions_ValidRequest_ShouldReturnOk() throws Exception {
        // Given
        TransactionSearchDto searchDto = new TransactionSearchDto("2023-10", 0, 10, "EUR");
        
        when(transactionService.getTransactions(eq("P-0123456789"), any(TransactionSearchDto.class)))
            .thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(post("/api/v1/transactions/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    @WithMockUser(username = "P-0123456789", roles = "CUSTOMER")
    void searchTransactions_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        TransactionSearchDto invalidSearchDto = new TransactionSearchDto();
        invalidSearchDto.setYearMonth("invalid");
        invalidSearchDto.setBaseCurrency("INVALID");
        
        // When & Then
        mockMvc.perform(post("/api/v1/transactions/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidSearchDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
