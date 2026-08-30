package com.bookstore.audit.service;

import com.bookstore.audit.entity.AuditLog;
import com.bookstore.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the audit service.
 *
 * <p>
 * Responsible for persisting audit records through the
 * {@link AuditLogRepository}.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Persists an audit log record.
     *
     * @param auditLog audit record to save
     * @return persisted audit record
     */
    @Override
    public AuditLog save(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }
}