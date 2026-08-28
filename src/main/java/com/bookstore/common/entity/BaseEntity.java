package com.bookstore.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Base entity that provides common persistence fields shared by all
 * database entities in the Bookstore application.
 *
 * <p>
 * This class is not mapped to its own database table. Instead, it is
 * declared as a {@link MappedSuperclass}, allowing its fields to be
 * inherited by concrete JPA entities.
 * </p>
 *
 * <p>
 * The entity provides:
 * <ul>
 *     <li>A UUID-based primary key.</li>
 *     <li>Creation timestamp.</li>
 *     <li>Last-update timestamp.</li>
 *     <li>Optimistic locking through the version field.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Optimistic locking is particularly important for operations such as
 * inventory updates and order processing where concurrent transactions
 * may attempt to modify the same record.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * Unique identifier of the entity.
     *
     * <p>
     * UUID is used instead of a sequential numeric identifier to make
     * identifiers harder to guess and safer to expose through REST APIs.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Timestamp representing when the entity was first persisted.
     */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp representing the most recent update to the entity.
     */
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Version number used by JPA/Hibernate for optimistic locking.
     *
     * <p>
     * Hibernate increments this value whenever the entity is updated.
     * Concurrent updates using an outdated version will be rejected
     * instead of silently overwriting another transaction's changes.
     * </p>
     */
    @Version
    @Column(nullable = false)
    private Long version;

    /**
     * Initializes audit timestamps immediately before a new entity
     * is inserted into the database.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.version == null) {
            this.version = 0L;
        }
    }

    /**
     * Updates the modification timestamp immediately before an existing
     * entity is updated in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}