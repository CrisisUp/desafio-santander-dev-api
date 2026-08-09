package me.dio.service;

import me.dio.config.AuthenticatedUser;
import me.dio.config.SecurityUtils;
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

    /**
     * Records an audit entry. The actor is read from the SecurityContext: when
     * authenticated, actor_user_id/actor_name carry the caller; otherwise they
     * fall back to "system".
     */
    @Transactional
    public void log(String action, String targetEntity, Long targetId, String details) {
        AuthenticatedUser actor = SecurityUtils.currentUser();
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setActorUserId(actor != null ? actor.userId() : null);
        log.setActorName(actor != null ? actor.username() : "system");
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    /**
     * Legacy overload: explicit actor (kept for tests / callers that pass one).
     */
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

    @Transactional(readOnly = true)
    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}