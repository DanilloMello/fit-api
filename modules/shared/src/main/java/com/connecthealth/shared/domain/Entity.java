package com.connecthealth.shared.domain;

import java.util.Objects;

/**
 * Base class for all domain entities.
 * Entities are defined by their identity, not their attributes.
 *
 * @param <ID> the type of the entity identifier
 */
public abstract class Entity<ID> {

    private ID id;

    protected Entity() {
        // Required by JPA
    }

    protected Entity(ID id) {
        this.id = id;
    }

    public ID getId() {
        return id;
    }

    protected void setId(ID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
