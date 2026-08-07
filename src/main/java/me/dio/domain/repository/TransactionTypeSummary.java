package me.dio.domain.repository;

import java.math.BigDecimal;

import me.dio.domain.model.TransactionType;

/**
 * Projection of the per-type aggregate (SUM(amount), COUNT) produced by the
 * GROUP BY query in TransactionRepository.
 */
public record TransactionTypeSummary(TransactionType type, BigDecimal total, Long count) {
}
