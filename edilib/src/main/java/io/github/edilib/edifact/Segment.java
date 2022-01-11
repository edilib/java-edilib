package io.github.edilib.edifact;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Segment {

    private Tag tag;

    private List<Value> dataElements;
}
