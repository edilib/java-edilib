package io.github.edilib.edifact;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class Location {
    private final String filename;
    private final int line;
    private final int column;

    @Override
    public String toString() {
        return String.format("%s@%s:%s", filename, line, column);
    }
}
