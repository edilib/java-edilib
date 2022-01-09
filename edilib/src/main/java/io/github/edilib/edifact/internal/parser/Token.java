package io.github.edilib.edifact.internal.parser;

import io.github.edilib.edifact.Value;
import io.github.edilib.edifact.Location;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Token {

    public final Location location;

    public final TokenType type;

    public final Value value;

    public Token(Location location, TokenType type, Value value) {
        this.location = location;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("type=%s, value=%s, location=%s", type, value, location);
    }
}
