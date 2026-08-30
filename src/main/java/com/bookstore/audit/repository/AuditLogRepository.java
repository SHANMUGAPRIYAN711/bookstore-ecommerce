package com.bookstore.audit.repository;

import com.bookstore.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for persistent audit log records.
 *
 * <p>
 * Provides CRUD operations for {@link AuditLog} entities and
 * allows Spring Data JPA to manage audit records in the database.
 * </p>
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}