package io.github.edilib.edifact.dom;

import io.github.edilib.edifact.stream.Format;
import io.github.edilib.edifact.stream.Token;
import io.github.edilib.edifact.stream.TokenReader;
import io.github.edilib.edifact.stream.TokenType;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class SegmentReader {

    private final List<Segment> segments = new LinkedList<>();
    private final TokenReader rd;

    public SegmentReader(String filename, Reader rd, Format format) {
        this(new TokenReader(filename, rd, format));
    }

    public SegmentReader(TokenReader rd) {
        this.rd = rd;
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

        if (rd.peek().type == TokenType.EOF) {
            return;
        }

        Token beginSegmentToken = consume(TokenType.BEGIN_SEGMENT);
        Tag tag = new Tag(beginSegmentToken.value);
        List<Value> dataElements = new ArrayList<>();
        while (rd.peek().type == TokenType.SIMPLE_DATA
                || rd.peek().type == TokenType.COMPOSITE_DATA
                || rd.peek().type == TokenType.REPEATED_DATA) {
            Token dataToken = rd.next();
            dataElements.add(dataToken.value);
        }
        consume(TokenType.END_SEGMENT);
        segments.add(new Segment(tag, dataElements));
    }

    private Token consume(TokenType expectedType) throws IOException {
        Token next = rd.next();
        if (next.type != expectedType) {
            throw new IOException("Expected " + expectedType + ", but was " + next + ".");
        }

        return next;
    }
}
