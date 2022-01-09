package io.github.edilib.edifact.stream;

public enum TokenType {
    BEGIN_SEGMENT,
    SIMPLE_DATA,
    COMPOSITE_DATA,
    REPEATED_DATA,
    END_SEGMENT,
    EOF
}
