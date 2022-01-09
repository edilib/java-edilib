package io.github.edilib.edifact;

import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
public class RepetitionValue implements Value {

    public final List<SimpleValue> values;

    public RepetitionValue(List<SimpleValue> values) {
        this.values = values;
    }

    public RepetitionValue(String... stringValues) {
        this(Arrays.stream(stringValues)
                .map((s) -> new SimpleValue(s, null, null))
                .collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return this.values.toString();
    }
}
