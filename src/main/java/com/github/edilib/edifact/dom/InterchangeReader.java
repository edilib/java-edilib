package com.github.edilib.edifact.dom;

import com.github.edilib.edifact.stream.TokenReader;
import com.github.edilib.edifact.stream.Token;
import com.github.edilib.edifact.stream.TokenType;
import com.github.edilib.edifact.stream.Format;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class InterchangeReader {

    private TokenReader parser;

    public InterchangeReader(String filename, Reader in, Format format) {
        parser = new TokenReader(filename, in, format);
    }

    public Interchange read() throws IOException {
        List<Segment> segments = readSegments();
        return new Interchange(segments);
    }

    private List<Segment> readSegments() throws IOException {
        List<Segment> segments = new ArrayList<>();
        while (parser.peek().type == TokenType.BEGIN_SEGMENT) {
            Segment segment = readSegment();
            segments.add(segment);
        }
        return segments;
    }

    private Segment readSegment() throws IOException {
        Token beginSegmentToken = consume(TokenType.BEGIN_SEGMENT);
        ;
        Tag tag = new Tag(beginSegmentToken.value);
        List<Value> dataElements = new ArrayList<>();
        while (parser.peek().type == TokenType.SIMPLE_DATA
                || parser.peek().type == TokenType.COMPOSITE_DATA
                || parser.peek().type == TokenType.REPEATED_DATA) {
            Token dataToken = parser.next();
            dataElements.add(dataToken.value);
        }
        consume(TokenType.END_SEGMENT);
        return new Segment(tag, dataElements);
    }

    private Token consume(TokenType expectedType) throws IOException {
        Token next = parser.next();
        if (next.type != expectedType) {
            throw new IOException("Expected " + expectedType + ", but was " + next + ".");
        }

        return next;
    }
}
