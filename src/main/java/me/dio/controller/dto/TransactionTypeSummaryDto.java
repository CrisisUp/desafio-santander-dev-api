package me.dio.controller.dto;

import java.math.BigDecimal;

import me.dio.domain.model.TransactionType;
import me.dio.domain.repository.TransactionTypeSummary;

public record TransactionTypeSummaryDto(TransactionType type, BigDecimal total, Long count) {

    public TransactionTypeSummaryDto(TransactionTypeSummary s) {
        this(s.type(), s.total(), s.count());
    }
}
