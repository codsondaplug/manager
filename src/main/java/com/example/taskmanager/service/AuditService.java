package com.example.taskmanager.service;

import com.example.taskmanager.model.AuditLog;
import com.example.taskmanager.repository.AuditLogRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository auditLogs;

    public AuditService(AuditLogRepository auditLogs) {
        this.auditLogs = auditLogs;
    }

    @Transactional
    public void record(String action, Long taskId, String details) {
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogs.save(new AuditLog(action, actor, taskId, details));
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAll() {
        return auditLogs.findAllByOrderByCreatedAtDesc();
    }
}
