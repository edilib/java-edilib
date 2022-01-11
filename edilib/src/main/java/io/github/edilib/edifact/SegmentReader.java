package io.github.edilib.edifact;

import io.github.edilib.edifact.internal.scanner.Scanner;
import io.github.edilib.edifact.internal.scanner.ScannerToken;
import io.github.edilib.edifact.internal.scanner.ScannerTokenType;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class SegmentReader {

    enum State {
        INITIAL,
        INITIAL_UNA_SEEN,
        IN_MESSAGE;
    }

    private final List<Segment> segments = new LinkedList<>();
    private final Scanner rd;
    private State state = State.INITIAL;

    public SegmentReader(String filename, Reader rd, Format format) {
        this.rd = new Scanner(filename, rd, format);
    }

    public List<Segment> readAll() throws IOException {
        List<Segment> segments = new ArrayList<>();
        while (hasNext()) {
            Segment segment = next();
            segments.add(segment);
        }

        return segments;
    }

    public boolean hasNext() throws IOException {
        fill();

        return !segments.isEmpty();
    }

    public Segment peek() throws IOException {
        fillAndCheckNotEmpty();

        return segments.get(0);
    }

    public Segment next() throws IOException {
        fillAndCheckNotEmpty();

        return segments.remove(0);
    }

    private void fillAndCheckNotEmpty() throws IOException {
        fill();

        if (segments.isEmpty()) {
            throw new NoSuchElementException("No more segments.");
        }
    }

    private void fill() throws IOException {
        if (!segments.isEmpty()) {
            return;
        }

        while (true) {
            switch (state) {
                case INITIAL:
                    if (rd.peek().type == ScannerTokenType.UNA_TAG) {
                        readUnaSegment();
                        state = State.INITIAL_UNA_SEEN;
                    } else if (rd.peek().type == ScannerTokenType.EOF) {
                        throw new ParseException("Empty file.", rd.peek().pos);
                    } else {
                        state = State.IN_MESSAGE;
                    }
                    break;
                case INITIAL_UNA_SEEN:
                    if (rd.peek().type == ScannerTokenType.UNA_TAG) {
                        throw new ParseException("Duplicate UNA segment seen.", rd.peek().pos);
                    } else if (rd.peek().type == ScannerTokenType.EOF) {
                        throw new ParseException("No segments after UNA segment found.", rd.peek().pos);
                    } else {
                        state = State.IN_MESSAGE;
                    }
                    break;
                case IN_MESSAGE:
                    if (rd.peek().type == ScannerTokenType.EOF) {
                        return;
                    } else if (rd.peek().type == ScannerTokenType.VALUE) {
                        readSegment();
                        return;
                    } else {
                        throw unexpectedInput(rd.next());
                    }
                default:
                    throw new IllegalStateException("Illegal state: " + state + ".");
            }
        }
    }

    private void readUnaSegment() throws IOException {
        consume(ScannerTokenType.UNA_TAG);
    }

    private IOException unexpectedInput(ScannerToken next) {
        return new ParseException("Unexpected token: " + next.type + ", value=" + next.stringValue, next.pos);
    }

    private void readSegment() throws IOException {
        if (follows(ScannerTokenType.EOF)) {
            return;
        }

        Tag tag = readSegmentTag();
        List<Value> dataElements = readSegmentDataElements();
        readSegmentEnd();

        segments.add(new Segment(tag, dataElements));
    }

    private void readSegmentEnd() throws IOException {
        consume(ScannerTokenType.SEGMENT_TERMINATOR);
    }

    private ScannerToken consume(ScannerTokenType... expectedTypes) throws IOException {
        ScannerToken next = rd.next();
        if (!asList(expectedTypes).contains(next.type)) {
            throw unexpectedInput(next);
        }
        return next;
    }

    private List<Value> readSegmentDataElements() throws IOException {
        List<Value> dataElements = new ArrayList<>();
        while (follows(ScannerTokenType.DATA_ELEMENT_SEPERATOR)) {
            Value dataElement = readDataElement();
            dataElements.add(dataElement);
        }
        return dataElements;
    }

    private Value readDataElement() throws IOException {
        List<Value> values = new ArrayList<>();
        consume(ScannerTokenType.DATA_ELEMENT_SEPERATOR);
        final int IN_SIMPLE_VALUE = 0;
        final int IN_COMPOSITE_VALUE = 1;
        final int IN_REPETITION_VALUE = 2;
        int state = IN_SIMPLE_VALUE;
        boolean valueSeen = false;
        while (true) {
            if (follows(ScannerTokenType.VALUE)) {
                ScannerToken valueToken = consume(ScannerTokenType.VALUE);
                values.add(new SimpleValue(valueToken.stringValue, valueToken.integerValue, valueToken.decimalValue));
                valueSeen = true;
            } else if ((state == IN_SIMPLE_VALUE || state == IN_COMPOSITE_VALUE)
                    && follows(ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR)) {
                state = IN_COMPOSITE_VALUE;
                if (!valueSeen) {
                    values.add(new SimpleValue("", null, null));
                }
                valueSeen = false;
                consume(ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR);
            } else if ((state == IN_SIMPLE_VALUE || state == IN_REPETITION_VALUE)
                    && follows(ScannerTokenType.REPETITION_SEPERATOR)) {
                state = IN_REPETITION_VALUE;
                if (!valueSeen) {
                    values.add(new SimpleValue("", null, null));
                }
                valueSeen = false;
                consume(ScannerTokenType.REPETITION_SEPERATOR);
            } else if (follows(ScannerTokenType.DATA_ELEMENT_SEPERATOR, ScannerTokenType.SEGMENT_TERMINATOR)) {
                if (!valueSeen) {
                    values.add(new SimpleValue("", null, null));
                }

                if (state == IN_SIMPLE_VALUE) {
                    return values.get(0);
                } else if (state == IN_COMPOSITE_VALUE) {
                    return new ComponentValue(values.stream().map((c) -> (SimpleValue) c).collect(Collectors.toList()));
                } else if (state == IN_REPETITION_VALUE) {
                    return new RepetitionValue(values.stream().map((c) -> (SimpleValue) c).collect(Collectors.toList()));
                } else {
                    throw new IllegalStateException(String.valueOf(state));
                }
            } else {
                throw unexpectedInput(rd.next());
            }
        }
    }

    private boolean follows(ScannerTokenType... types) throws IOException {
        return asList(types).contains(rd.peek().type);
    }

    private Tag readSegmentTag() throws IOException {

        ScannerToken valueToken = consume(ScannerTokenType.VALUE);
        List<SimpleValue> componentValues = new ArrayList<>();
        boolean valueSeen = false;
        if (follows(ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR)) {
            consume(ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR);
            while (true) {
                if (follows(ScannerTokenType.VALUE)) {
                    ScannerToken componentValueToken = consume(ScannerTokenType.VALUE);
                    componentValues.add(new SimpleValue(componentValueToken.stringValue, componentValueToken.integerValue, componentValueToken.decimalValue));
                    valueSeen = true;
                } else if (follows(ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR)
                        || follows(ScannerTokenType.DATA_ELEMENT_SEPERATOR)) {
                    if (!valueSeen) {
                        componentValues.add(new SimpleValue("", null, null));
                    }
                    consume(ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR,
                            ScannerTokenType.DATA_ELEMENT_SEPERATOR,
                            ScannerTokenType.SEGMENT_TERMINATOR);
                    valueSeen = false;
                } else if (follows(ScannerTokenType.SEGMENT_TERMINATOR, ScannerTokenType.DATA_ELEMENT_SEPERATOR)) {
                    if (!valueSeen) {
                        componentValues.add(new SimpleValue("", null, null));
                    }
                    break;
                } else {
                    throw unexpectedInput(rd.next());
                }
            }
        }

        return new Tag(valueToken.stringValue, componentValues);
    }
}
