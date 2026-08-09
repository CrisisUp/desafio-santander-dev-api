package me.dio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.dio.controller.dto.AuditLogDto;
import me.dio.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only audit log access. Restricted to ADMIN in SecurityConfig.
 */
@RestController
@RequestMapping("/audit")
@Tag(name = "Audit Controller", description = "Read-only audit log (ADMIN only).")
public record AuditController(AuditService auditService) {

    @GetMapping
    @Operation(summary = "List audit entries", description = "Optionally filter by actor user id or target entity+id.")
    public ResponseEntity<List<AuditLogDto>> list(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String targetEntity,
            @RequestParam(required = false) Long targetId) {
        List<AuditLogDto> entries;
        if (actorId != null) {
            entries = auditService.findByActor(actorId).stream().map(AuditLogDto::new).toList();
        } else if (targetEntity != null && targetId != null) {
            entries = auditService.findByTarget(targetEntity, targetId).stream().map(AuditLogDto::new).toList();
        } else {
            entries = auditService.findAll().stream().map(AuditLogDto::new).toList();
        }
        return ResponseEntity.ok(entries);
    }
}
