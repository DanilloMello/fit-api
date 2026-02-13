package com.connecthealth.shared.domain;

import java.time.Instant;

/**
 * Base interface for all domain events.
 * Domain events represent something meaningful that happened in the domain.
 */
public interface DomainEvent {

    /**
     * Returns the time at which the event occurred.
     *
     * @return the event timestamp
     */
    Instant occurredOn();
}
