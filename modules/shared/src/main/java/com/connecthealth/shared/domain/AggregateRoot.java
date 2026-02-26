package com.connecthealth.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for aggregate roots.
 * An aggregate root is the entry point to an aggregate and is responsible
 * for maintaining the consistency boundary of the aggregate.
 * It collects domain events that occurred during the aggregate's lifecycle.
 *
 * @param <ID> the type of the aggregate root identifier
 */
public abstract class AggregateRoot<ID> extends Entity<ID> {

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot() {
        super();
    }

    protected AggregateRoot(ID id) {
        super(id);
    }

    /**
     * Registers a domain event to be published when the aggregate is persisted.
     *
     * @param event the domain event to register
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Returns all domain events that have been registered on this aggregate.
     *
     * @return an unmodifiable list of domain events
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clears all registered domain events.
     * Should be called after events have been published.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
