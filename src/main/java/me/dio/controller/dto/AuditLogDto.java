package me.dio.controller.dto;

import me.dio.domain.model.AuditLog;

import java.time.LocalDateTime;

public record AuditLogDto(
        Long id,
        String action,
        Long actorUserId,
        String actorName,
        String targetEntity,
        Long targetId,
        String details,
        LocalDateTime createdAt) {

    public AuditLogDto(AuditLog log) {
        this(log.getId(), log.getAction(), log.getActorUserId(), log.getActorName(),
                log.getTargetEntity(), log.getTargetId(), log.getDetails(), log.getCreatedAt());
    }
}
