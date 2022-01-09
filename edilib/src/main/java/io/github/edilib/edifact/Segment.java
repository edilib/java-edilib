package io.github.edilib.edifact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@ToString
public class Segment {

    private Tag tag;

    private List<Value> dataElements;
}
