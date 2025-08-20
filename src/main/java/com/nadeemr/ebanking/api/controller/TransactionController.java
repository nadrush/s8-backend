package com.nadeemr.ebanking.api.controller;

import com.nadeemr.ebanking.api.dto.TransactionPageDto;
import com.nadeemr.ebanking.api.dto.TransactionSearchDto;
import com.nadeemr.ebanking.domain.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for transaction operations
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Validated
@Tag(name = "Transactions", description = "Transaction management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @GetMapping
    @Operation(
        summary = "Get paginated transactions for the authenticated customer",
        description = "Returns a paginated list of transactions for a specific calendar month with currency conversion"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved transactions",
            content = @Content(mediaType = "application/json", 
                             schema = @Schema(implementation = TransactionPageDto.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<TransactionPageDto> getTransactions(
            @Parameter(description = "Year and month in YYYY-MM format", required = true, example = "2023-10")
            @RequestParam("yearMonth") 
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Date must be in YYYY-MM format") 
            String yearMonth,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") 
            int page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", defaultValue = "20") 
            int size,
            
            @Parameter(description = "Base currency for conversion", example = "EUR")
            @RequestParam(value = "baseCurrency", defaultValue = "EUR") 
            @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
            String baseCurrency,
            
            @Parameter(description = "Filter by account IBAN (optional)")
            @RequestParam(value = "accountIban", required = false)
            @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$", message = "Invalid IBAN format")
            String accountIban) {
        
        // Get authenticated customer ID from JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();
        
        logger.info("Getting transactions for customer {} for month {} (page: {}, size: {})", 
                   customerId, yearMonth, page, size);
        
        // Create search criteria
        TransactionSearchDto searchCriteria = new TransactionSearchDto(yearMonth, page, size, baseCurrency);
        searchCriteria.setAccountIban(accountIban);
        
        // Get transactions
        TransactionPageDto result = transactionService.getTransactions(customerId, searchCriteria);
        
        logger.info("Retrieved {} transactions for customer {} in month {}", 
                   result.getTransactions().size(), customerId, yearMonth);
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/search")
    @Operation(
        summary = "Search transactions with advanced criteria",
        description = "Search transactions using POST method for complex search criteria"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved transactions",
            content = @Content(mediaType = "application/json", 
                             schema = @Schema(implementation = TransactionPageDto.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid search criteria",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<TransactionPageDto> searchTransactions(
            @Parameter(description = "Transaction search criteria", required = true)
            @Valid @RequestBody TransactionSearchDto searchCriteria) {
        
        // Get authenticated customer ID from JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();
        
        logger.info("Searching transactions for customer {} with criteria: {}", customerId, searchCriteria);
        
        // Get transactions
        TransactionPageDto result = transactionService.getTransactions(customerId, searchCriteria);
        
        logger.info("Found {} transactions for customer {} with search criteria", 
                   result.getTransactions().size(), customerId);
        
        return ResponseEntity.ok(result);
    }
}
