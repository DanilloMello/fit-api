package com.connecthealth.shared.domain;

/**
 * Base class for value objects.
 * Value objects are defined by their attributes rather than an identity.
 * Subclasses must implement equals() and hashCode() based on their attributes.
 */
public abstract class ValueObject {

    /**
     * Value objects must implement equality based on their attributes.
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * Value objects must implement hashCode based on their attributes.
     */
    @Override
    public abstract int hashCode();
}
