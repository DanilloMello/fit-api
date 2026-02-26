package com.connecthealth.shared.domain;

import java.util.List;

/**
 * Interface for publishing domain events.
 * Implementations should handle the actual dispatching of events
 * to registered handlers/listeners.
 */
public interface DomainEventPublisher {

    /**
     * Publishes a single domain event.
     *
     * @param event the domain event to publish
     */
    void publish(DomainEvent event);

    /**
     * Publishes a list of domain events.
     *
     * @param events the domain events to publish
     */
    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
