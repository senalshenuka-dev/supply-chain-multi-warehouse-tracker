package com.supplychainmultiwarehousetracker.service;

import com.supplychainmultiwarehousetracker.domain.model.AuditLog;
import com.supplychainmultiwarehousetracker.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String entityName, String entityId, String action, String performedBy, String details) {
        AuditLog log = AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy != null ? performedBy : "SYSTEM")
                .details(details)
                .build();
        auditLogRepository.save(log);
    }
}
