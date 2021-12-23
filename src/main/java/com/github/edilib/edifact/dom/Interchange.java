package com.github.edilib.edifact.dom;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Interchange {
    private List<Segment> segments;
}
