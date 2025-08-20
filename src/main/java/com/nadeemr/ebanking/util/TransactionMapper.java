package com.nadeemr.ebanking.util;

import com.nadeemr.ebanking.api.dto.TransactionDto;
import com.nadeemr.ebanking.domain.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Transaction entities and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    
    TransactionDto toDto(Transaction transaction);
    
    Transaction toEntity(TransactionDto transactionDto);
}
