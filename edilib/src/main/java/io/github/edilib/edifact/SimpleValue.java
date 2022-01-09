package io.github.edilib.edifact;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@AllArgsConstructor
@EqualsAndHashCode
public class SimpleValue implements Value {

    public final String stringValue;

    public final Integer integerValue;

    public final BigDecimal decimalValue;

    public SimpleValue(String stringValue) {
        this(stringValue, null, null);
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
