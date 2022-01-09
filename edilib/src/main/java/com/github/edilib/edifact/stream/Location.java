package com.github.edilib.edifact.stream;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor
public class Location {
    private final String filename;
    private final int line;
    private final int column;

    @Override
    public String toString() {
        return String.format("%s@%s:%s", filename, line, column);
    }
}
