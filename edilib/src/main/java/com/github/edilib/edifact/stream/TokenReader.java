package com.github.edilib.edifact.stream;

import com.github.edilib.edifact.dom.ComponentValue;
import com.github.edilib.edifact.dom.RepetitionValue;
import com.github.edilib.edifact.dom.SimpleValue;
import com.github.edilib.edifact.dom.Value;
import com.github.edilib.edifact.stream.scanner.Scanner;
import com.github.edilib.edifact.stream.scanner.ScannerToken;
import com.github.edilib.edifact.stream.scanner.ScannerTokenType;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;

public class TokenReader {

    @FunctionalInterface
    private interface Action {
        void act(State state, ScannerToken scannerToken, TokenReader parser) throws IOException;
    }

    @AllArgsConstructor
    private static class Rule {
        private State state;
        private ScannerTokenType type;
        private Action action;
        private State newState;

        public boolean matches(State state, ScannerToken token) {
            return (this.state == null || state == this.state) && (this.type == null || type == token.type);
        }

        public void apply(State state, ScannerToken token, TokenReader parser) throws IOException {
            action.act(state, token, parser);
            parser.state = newState;
        }
    }

    private static final Rule DEFAULT_RULE = new Rule(null, null, TokenReader::unexpectedInput, null);
    private static final List<Rule> RULES = asList(
            new Rule(State.INITIAL, ScannerTokenType.UNA_TAG, TokenReader::ignore, State.INITIAL_UNA_SEEN),
            new Rule(State.INITIAL, ScannerTokenType.VALUE, TokenReader::beginSegmentTagValue, State.IN_SEGMENT_TAG),

            new Rule(State.INITIAL_UNA_SEEN, ScannerTokenType.VALUE, TokenReader::beginSegmentTagValue, State.IN_SEGMENT_TAG),

            new Rule(State.IN_SEGMENT_TAG, ScannerTokenType.DATA_ELEMENT_SEPERATOR, TokenReader::returnBeginSegmentParserToken, State.IN_SEGMENT_TAG_DATA_ELEMENT_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_TAG, ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR, TokenReader::ignore, State.IN_SEGMENT_TAG_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_TAG, ScannerTokenType.SEGMENT_TERMINATOR, TokenReader::returnBeginSegmentParserTokenAndEndSegment, State.INITIAL_SEGMENT_SEEN),

            new Rule(State.IN_SEGMENT_TAG_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.VALUE, TokenReader::appendSegmentTagValue, State.IN_SEGMENT_TAG),

            new Rule(State.IN_SEGMENT_TAG_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.VALUE, TokenReader::beginDataElementAndAppendDataElementValue, State.IN_SEGMENT_DATA),
            new Rule(State.IN_SEGMENT_TAG_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.SEGMENT_TERMINATOR, TokenReader::returnEmptyDataElementAndEndSegment, State.INITIAL_SEGMENT_SEEN),
            new Rule(State.IN_SEGMENT_TAG_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.DATA_ELEMENT_SEPERATOR, TokenReader::returnEmptySegmentData, State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_TAG_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR, TokenReader::beginDataElementAndAppendEmptyComponentData, State.IN_SEGMENT_DATA_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_TAG_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.REPETITION_SEPERATOR, TokenReader::beginDataElementAndAppendEmptyRepeatedDataElement, State.IN_SEGMENT_DATA_REPETITION_SEPERATOR_SEEN),

            new Rule(State.IN_SEGMENT_DATA, ScannerTokenType.SEGMENT_TERMINATOR, TokenReader::returnSegmentDataAndEndSegment, State.INITIAL_SEGMENT_SEEN),
            new Rule(State.IN_SEGMENT_DATA, ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR, TokenReader::ignore, State.IN_SEGMENT_DATA_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_DATA, ScannerTokenType.DATA_ELEMENT_SEPERATOR, TokenReader::returnElementData, State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_DATA, ScannerTokenType.REPETITION_SEPERATOR, TokenReader::ignore, State.IN_SEGMENT_DATA_REPETITION_SEPERATOR_SEEN),



            new Rule(State.IN_SEGMENT_DATA_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.VALUE, TokenReader::appendComponentSegmentDataValue, State.IN_SEGMENT_DATA),
            new Rule(State.IN_SEGMENT_DATA_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.SEGMENT_TERMINATOR, TokenReader::appendEmptyComponentDataElementReturnDataElementAndEndSegment, State.INITIAL_SEGMENT_SEEN),
            new Rule(State.IN_SEGMENT_DATA_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.DATA_ELEMENT_SEPERATOR, TokenReader::appendEmptyComponentDataAndReturnDataElement, State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_DATA_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR, TokenReader::appendEmptyComponentData, State.IN_SEGMENT_DATA_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN),

            new Rule(State.IN_SEGMENT_DATA_REPETITION_SEPERATOR_SEEN, ScannerTokenType.VALUE, TokenReader::appendRepeatedSegmentDataValue, State.IN_SEGMENT_DATA),
            new Rule(State.IN_SEGMENT_DATA_REPETITION_SEPERATOR_SEEN, ScannerTokenType.SEGMENT_TERMINATOR, TokenReader::appendEmptyRepeatedSegmentDataValueAndEndSegment, State.INITIAL_SEGMENT_SEEN),
            new Rule(State.IN_SEGMENT_DATA_REPETITION_SEPERATOR_SEEN, ScannerTokenType.REPETITION_SEPERATOR, TokenReader::appendEmptyRepeatedSegmentDataValue, State.IN_SEGMENT_DATA_REPETITION_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_DATA_REPETITION_SEPERATOR_SEEN, ScannerTokenType.DATA_ELEMENT_SEPERATOR, TokenReader::appendEmptyRepeatedSegmentDataValueAndReturnDataElement, State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN),

            new Rule(State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.VALUE, TokenReader::beginDataElementAndAppendDataElementValue, State.IN_SEGMENT_DATA),
            new Rule(State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.SEGMENT_TERMINATOR, TokenReader::returnEmptyDataElementAndEndSegment, State.INITIAL_SEGMENT_SEEN),
            new Rule(State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.REPETITION_SEPERATOR, TokenReader::beginDataElementAndAppendEmptyRepeatedDataElement, State.IN_SEGMENT_DATA_REPETITION_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.DATA_ELEMENT_SEPERATOR, TokenReader::returnEmptySegmentData, State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN),
            new Rule(State.IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN, ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR, TokenReader::beginDataElementAndAppendEmptyComponentData, State.IN_SEGMENT_DATA_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN),

            new Rule(State.INITIAL_SEGMENT_SEEN, ScannerTokenType.EOF, TokenReader::ignore, State.END),
            new Rule(State.INITIAL_SEGMENT_SEEN, ScannerTokenType.VALUE, TokenReader::beginSegmentTagValue, State.IN_SEGMENT_TAG),

            new Rule(State.END, ScannerTokenType.EOF, TokenReader::eof, State.END)
    );

    enum State {
        INITIAL,
        INITIAL_UNA_SEEN,
        INITIAL_SEGMENT_SEEN,
        IN_SEGMENT_TAG,
        IN_SEGMENT_TAG_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN,
        IN_SEGMENT_TAG_DATA_ELEMENT_SEPERATOR_SEEN,
        IN_SEGMENT_DATA,
        IN_SEGMENT_DATA_DATA_ELEMENT_SEPERATOR_SEEN,
        IN_SEGMENT_DATA_COMPONENT_DATA_ELEMENT_SEPERATOR_SEEN,
        IN_SEGMENT_DATA_REPETITION_SEPERATOR_SEEN,
        END
    }

    private final com.github.edilib.edifact.stream.scanner.Scanner scanner;
    private final ValuesCollector tagBuilder = new ValuesCollector();
    private final ValuesCollector valueBuilder = new ValuesCollector();

    private final List<Token> tokens = new LinkedList<>();
    private State state = State.INITIAL;

    public TokenReader(Reader reader, Format format) {
        this("<unknown>", reader, format);
    }

    public TokenReader(String filename, Reader reader, Format format) {
        this.scanner = new Scanner(filename, reader, format);
    }

    public Token peek() throws IOException {

        if (tokens.isEmpty()) {
            fill();
        }

        return tokens.get(0);
    }

    public Token next() throws IOException {

        if (tokens.isEmpty()) {
            fill();
        }

        return tokens.remove(0);
    }

    private void fill() throws IOException {
        while (tokens.isEmpty()) {
            ScannerToken next = scanner.next();
            Rule rule = RULES.stream()
                    .filter((r) -> r.matches(state, next))
                    .findFirst()
                    .orElse(DEFAULT_RULE);
            rule.apply(state, next, this);
        }
    }

    private static void appendEmptyComponentDataElementReturnDataElementAndEndSegment(State state, ScannerToken scannerToken, TokenReader parser) {
        appendEmptyComponentDataAndReturnDataElement(state, scannerToken, parser);
        endSegment(state, scannerToken, parser);
    }

    private static void appendEmptyComponentDataAndReturnDataElement(State state, ScannerToken scannerToken, TokenReader parser) {
        appendEmptyComponentData(state, scannerToken, parser);
        returnElementData(state, scannerToken, parser);
    }

    private static void appendEmptyComponentData(State state, ScannerToken scannerToken, TokenReader parser) {
        parser.valueBuilder.appendComponentValue("", null, null);
    }

    private static void beginDataElementAndAppendEmptyComponentData(State state, ScannerToken scannerToken, TokenReader parser) {
        beginDataElement(state, scannerToken, parser);
        appendEmptyComponentData(state, scannerToken, parser);
    }

    private static void beginDataElement(State state, ScannerToken scannerToken, TokenReader parser) {
        parser.valueBuilder.reset(scannerToken);
    }

    private static void beginDataElementAndAppendEmptyRepeatedDataElement(State state, ScannerToken scannerToken, TokenReader parser) {
        beginDataElement(state, scannerToken, parser);
        appendEmptyRepeatedSegmentDataValue(state, scannerToken, parser);
    }

    private static void appendEmptyRepeatedSegmentDataValueAndReturnDataElement(State state, ScannerToken scannerToken, TokenReader parser) {
        appendEmptyRepeatedSegmentDataValue(state, scannerToken, parser);
        returnElementData(state, scannerToken, parser);
    }

    private static void returnEmptyDataElementAndEndSegment(State state, ScannerToken scannerToken, TokenReader parser) {
        returnEmptySegmentData(state, scannerToken, parser);
        endSegment(state, scannerToken, parser);
    }

    private static void beginDataElementAndAppendDataElementValue(State state, ScannerToken token, TokenReader parser) {
        beginDataElement(state, token, parser);
        parser.valueBuilder.appendDataElementValue(token.stringValue, token.integerValue, token.decimalValue);
    }

    private static void beginSegmentTagValue(State state, ScannerToken token, TokenReader parser) {
        parser.tagBuilder.reset(token);
        parser.tagBuilder.appendTagValue(token.stringValue);
    }

    private static void ignore(State state, ScannerToken scannerToken, TokenReader parser) {
    }

    private static void appendSegmentTagValue(State state, ScannerToken token, TokenReader parser) {
        parser.tagBuilder.appendComponentValue(token.stringValue, token.integerValue, token.decimalValue);
    }

    private static void appendComponentSegmentDataValue(State state, ScannerToken token, TokenReader parser) {
        parser.valueBuilder.appendComponentValue(token.stringValue, token.integerValue, token.decimalValue);
    }

    private static void appendRepeatedSegmentDataValue(State state, ScannerToken token, TokenReader parser) {
        parser.valueBuilder.appendRepeatedValue(token.stringValue, token.integerValue, token.decimalValue);
    }

    private static void appendEmptyRepeatedSegmentDataValue(State state, ScannerToken token, TokenReader parser) {
        parser.valueBuilder.appendRepeatedValue("", null, null);
    }

    private static void appendEmptyRepeatedSegmentDataValueAndEndSegment(State state, ScannerToken token, TokenReader parser) {
        appendEmptyRepeatedSegmentDataValue(state, token, parser);
        returnElementData(state, token, parser);
        endSegment(state, token, parser);
    }

    private static void eof(State state, ScannerToken token, TokenReader parser) {
        parser.tokens.add(new Token(token.pos, TokenType.EOF, null));
    }

    private static void endSegment(State state, ScannerToken scannerToken, TokenReader parser) {
        parser.tokens.add(new Token(scannerToken.pos, TokenType.END_SEGMENT, null));
    }

    private static void returnBeginSegmentParserTokenAndEndSegment(State state, ScannerToken token, TokenReader parser) {
        returnBeginSegmentParserToken(state, token, parser);
        endSegment(state, token, parser);
    }

    private static void returnBeginSegmentParserToken(State state, ScannerToken token, TokenReader parser) {
        parser.tokens.add(parser.tagBuilder.buildParserToken(TokenType.BEGIN_SEGMENT));
    }

    private static void returnSegmentDataAndEndSegment(State state, ScannerToken scannerToken, TokenReader parser) {
        returnElementData(state, scannerToken, parser);
        endSegment(state, scannerToken, parser);
    }

    private static void returnEmptySegmentData(State state, ScannerToken token, TokenReader parser) {
        parser.tokens.add(new Token(token.pos, TokenType.SIMPLE_DATA, new SimpleValue("")));
    }

    private static void returnElementData(State state, ScannerToken token, TokenReader parser) {

        Token dataParserToken;
        if (parser.valueBuilder.valueType == SimpleValue.class) {
            dataParserToken = parser.valueBuilder.buildParserToken(TokenType.SIMPLE_DATA);
        } else if (parser.valueBuilder.valueType == ComponentValue.class) {
            dataParserToken = parser.valueBuilder.buildParserToken(TokenType.COMPOSITE_DATA);
        } else if (parser.valueBuilder.valueType == RepetitionValue.class) {
            dataParserToken = parser.valueBuilder.buildParserToken(TokenType.REPEATED_DATA);
        } else {
            throw new IllegalStateException("Unknown value type: " + parser.valueBuilder.valueType);
        }

        parser.tokens.add(dataParserToken);
    }

    private static void unexpectedInput(State state, ScannerToken token, TokenReader parser) throws IOException {
        throw new IOException(String.format("Unexpected input with type=%s, value='%s', state=%s, at %s. Expected one of %s.", token.type, token.stringValue, state, token.pos, parser.expectedInputIn(state)));
    }

    private Set<ScannerTokenType> expectedInputIn(State state) {
        Set<ScannerTokenType> allowedTokenTypes = new HashSet<>();
        for (Rule rule : RULES) {
            if (rule.state == state && rule.type != null) {
                allowedTokenTypes.add(rule.type);
            }
        }
        return allowedTokenTypes;
    }

    private static class ValuesCollector {
        private Class<? extends Value> valueType;
        private ScannerToken beginToken;
        private List<SimpleValue> values;

        public void reset(ScannerToken scannerToken) {
            beginToken = scannerToken;
            values = new ArrayList<>();
        }

        public void appendTagValue(String stringValue) {
            if (!values.isEmpty()) {
                throw new IllegalStateException("Tag value already exists.");
            }
            valueType = SimpleValue.class;
            values.add(new SimpleValue(stringValue, null, null));
        }

        public void appendDataElementValue(String stringValue, Integer integerValue, BigDecimal decimalValue) {
            if (!values.isEmpty()) {
                throw new IllegalStateException("Data element value already exists.");
            }
            valueType = SimpleValue.class;
            values.add(new SimpleValue(stringValue, integerValue, decimalValue));
        }

        public void appendRepeatedValue(String stringValue, Integer integerValue, BigDecimal decimalValue) {
            valueType = RepetitionValue.class;
            values.add(new SimpleValue(stringValue, integerValue, decimalValue));
        }

        public void appendComponentValue(String stringValue, Integer integerValue, BigDecimal decimalValue) {
            valueType = ComponentValue.class;
            values.add(new SimpleValue(stringValue, integerValue, decimalValue));
        }

        public Token buildParserToken(TokenType type) {
            Value value = buildValue();
            Token parserToken = new Token(beginToken.pos, type, value);
            clear();
            return parserToken;
        }

        private void clear() {
            values = null;
            beginToken = null;
        }

        private Value buildValue() {
            if (values.isEmpty()) {
                throw new IllegalStateException("No values.");
            }

            if (valueType == SimpleValue.class) {
                if (values.size() != 1) {
                    throw new IllegalStateException("Multiple values.");
                }
                return values.get(0);
            } else if (valueType == ComponentValue.class) {
                return new ComponentValue(values);
            } else if (valueType == RepetitionValue.class) {
                return new RepetitionValue(values);
            } else {
                throw new IllegalStateException("Unknown value type: " + valueType);
            }
        }

    }
}
