package com.bookstore.audit.service;

import com.bookstore.audit.entity.AuditLog;

/**
 * Service responsible for persisting application audit records.
 */
public interface AuditService {

    /**
     * Persists an audit log record.
     *
     * @param auditLog audit information to persist
     * @return persisted audit log
     */
    AuditLog save(AuditLog auditLog);
}