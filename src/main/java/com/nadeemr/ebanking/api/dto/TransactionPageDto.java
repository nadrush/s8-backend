package com.nadeemr.ebanking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Schema(description = "Paginated transaction response with totals")
public class TransactionPageDto {
    
    @Schema(description = "List of transactions in this page")
    private List<TransactionDto> transactions;
    
    @Schema(description = "Pagination information")
    private PageInfo pageInfo;
    
    @Schema(description = "Summary information for the page")
    private TransactionSummary summary;
    
    public TransactionPageDto() {}
    
    public TransactionPageDto(List<TransactionDto> transactions, PageInfo pageInfo, TransactionSummary summary) {
        this.transactions = transactions;
        this.pageInfo = pageInfo;
        this.summary = summary;
    }
    
    // Getters and Setters
    public List<TransactionDto> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }
    
    public PageInfo getPageInfo() {
        return pageInfo;
    }
    
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
    
    public TransactionSummary getSummary() {
        return summary;
    }
    
    public void setSummary(TransactionSummary summary) {
        this.summary = summary;
    }
    
    @Schema(description = "Page information")
    public static class PageInfo {
        @Schema(description = "Current page number (0-based)", example = "0")
        private int page;
        
        @Schema(description = "Page size", example = "20")
        private int size;
        
        @Schema(description = "Total number of elements", example = "150")
        private long totalElements;
        
        @Schema(description = "Total number of pages", example = "8")
        private int totalPages;
        
        @Schema(description = "Whether this is the first page", example = "true")
        private boolean first;
        
        @Schema(description = "Whether this is the last page", example = "false")
        private boolean last;
        
        public PageInfo() {}
        
        public PageInfo(int page, int size, long totalElements, int totalPages, boolean first, boolean last) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
        }
        
        // Getters and Setters
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
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        
        public boolean isFirst() {
            return first;
        }
        
        public void setFirst(boolean first) {
            this.first = first;
        }
        
        public boolean isLast() {
            return last;
        }
        
        public void setLast(boolean last) {
            this.last = last;
        }
    }
    
    @Schema(description = "Transaction summary information")
    public static class TransactionSummary {
        @Schema(description = "Total credit amount in base currency", example = "1250.75")
        private BigDecimal totalCredit;
        
        @Schema(description = "Total debit amount in base currency", example = "890.25")
        private BigDecimal totalDebit;
        
        @Schema(description = "Net amount (credit - debit) in base currency", example = "360.50")
        private BigDecimal netAmount;
        
        @Schema(description = "Base currency for amounts", example = "EUR")
        private String baseCurrency;
        
        public TransactionSummary() {}
        
        public TransactionSummary(BigDecimal totalCredit, BigDecimal totalDebit, BigDecimal netAmount, String baseCurrency) {
            this.totalCredit = totalCredit;
            this.totalDebit = totalDebit;
            this.netAmount = netAmount;
            this.baseCurrency = baseCurrency;
        }
        
        // Getters and Setters
        public BigDecimal getTotalCredit() {
            return totalCredit;
        }
        
        public void setTotalCredit(BigDecimal totalCredit) {
            this.totalCredit = totalCredit;
        }
        
        public BigDecimal getTotalDebit() {
            return totalDebit;
        }
        
        public void setTotalDebit(BigDecimal totalDebit) {
            this.totalDebit = totalDebit;
        }
        
        public BigDecimal getNetAmount() {
            return netAmount;
        }
        
        public void setNetAmount(BigDecimal netAmount) {
            this.netAmount = netAmount;
        }
        
        public String getBaseCurrency() {
            return baseCurrency;
        }
        
        public void setBaseCurrency(String baseCurrency) {
            this.baseCurrency = baseCurrency;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionPageDto that = (TransactionPageDto) o;
        return Objects.equals(transactions, that.transactions) &&
                Objects.equals(pageInfo, that.pageInfo) &&
                Objects.equals(summary, that.summary);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactions, pageInfo, summary);
    }
}
