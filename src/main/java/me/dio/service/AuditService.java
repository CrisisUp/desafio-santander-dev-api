package me.dio.service;

import me.dio.domain.model.AuditLog;
import me.dio.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String action, Long actorUserId, String actorName,
                    String targetEntity, Long targetId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setActorUserId(actorUserId);
        log.setActorName(actorName);
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findByActor(Long actorUserId) {
        return auditLogRepository.findByActorUserIdOrderByCreatedAtDesc(actorUserId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findByTarget(String targetEntity, Long targetId) {
        return auditLogRepository.findByTargetEntityAndTargetIdOrderByCreatedAtDesc(targetEntity, targetId);
    }
}