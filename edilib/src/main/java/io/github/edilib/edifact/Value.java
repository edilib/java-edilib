package io.github.edilib.edifact;

public interface Value {
    default boolean isComponent() {
        return this instanceof ComponentValue;
    }

    default boolean isSimple() {
        return this instanceof SimpleValue;
    }

    default boolean isRepetition() {
        return this instanceof RepetitionValue;
    }
}
