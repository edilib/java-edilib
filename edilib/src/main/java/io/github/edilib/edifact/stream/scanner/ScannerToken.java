package io.github.edilib.edifact.stream.scanner;

import io.github.edilib.edifact.stream.Location;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode
@AllArgsConstructor
public class ScannerToken {
    public final Location pos;

    public final ScannerTokenType type;

    public final String stringValue;

    public final Integer integerValue;

    public final BigDecimal decimalValue;

    public final String message;

    @Override
    public String toString() {
        return String.format("Token{pos=%s, type=%s, stringValue=%s, message=%s}", pos, type, stringValue, message);
    }

}
