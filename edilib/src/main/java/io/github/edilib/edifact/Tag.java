package io.github.edilib.edifact;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class Tag {
    private String name;

    private List<SimpleValue> componentValues;

    public Tag(Value value) {
        if (value instanceof SimpleValue) {
            name = ((SimpleValue) value).stringValue;
        } else if (value instanceof ComponentValue) {
            ComponentValue compositeValue = (ComponentValue) value;
            name = compositeValue.values.get(0).stringValue;
            componentValues = new ArrayList<>(compositeValue.values.subList(1, compositeValue.values.size()));
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value);
        }
    }

    @Override
    public String toString() {
        return String.format("%s,%s", name, componentValues);
    }
}
