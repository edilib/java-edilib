package io.github.edilib.edifact.stream;

import io.github.edilib.edifact.dom.RepetitionValue;
import io.github.edilib.edifact.dom.SimpleValue;
import io.github.edilib.edifact.dom.ComponentValue;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenReaderTest {
    private TokenReader parser;

    private final List<Token> result = new ArrayList<>();

    @Test
    void detectsEmptyFile() {
        givenParserInput("");

        assertThrows(IOException.class, this::whenParsed, "Unexpected input with type=EOF, value='null', at <unknown>@0:0. Expected one of [VALUE, UNA_TAG].");
    }

    @Test
    void unaPrefixedSingleEmptySegment() throws IOException {
        givenParserInput("UNA:+.? 'UNH'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 9), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 12), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 13), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 13), TokenType.EOF, null));
    }

    @Test
    void singleEmptySegment() throws IOException {
        givenParserInput("UNH'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 3), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 4), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 4), TokenType.EOF, null));
    }


    @Test
    void singleSegmentWithSingleEmptyDataElement() throws IOException {
        givenParserInput("UNH+'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.SIMPLE_DATA, new SimpleValue("")),
                new Token(new Location("<unknown>", 0, 4), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 5), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 5), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithTwoEmptyDataElements() throws IOException {
        givenParserInput("UNH++'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.SIMPLE_DATA, new SimpleValue("")),
                new Token(new Location("<unknown>", 0, 5), TokenType.SIMPLE_DATA, new SimpleValue("")),
                new Token(new Location("<unknown>", 0, 5), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 6), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 6), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithThreeEmptyDataElements() throws IOException {
        givenParserInput("UNH+++'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.SIMPLE_DATA, new SimpleValue("")),
                new Token(new Location("<unknown>", 0, 5), TokenType.SIMPLE_DATA, new SimpleValue("")),
                new Token(new Location("<unknown>", 0, 6), TokenType.SIMPLE_DATA, new SimpleValue("")),
                new Token(new Location("<unknown>", 0, 6), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 7), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 7), TokenType.EOF, null));
    }


    @Test
    void singleSegmentWithSingleDataElementWithEmptyComponentDataElement() throws IOException {
        givenParserInput("UNH+:'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.COMPOSITE_DATA, new ComponentValue("", "")),
                new Token(new Location("<unknown>", 0, 5), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 6), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 6), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithSingleDataElementWithTwoEmptyComponentDataElements() throws IOException {
        givenParserInput("UNH+::'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.COMPOSITE_DATA, new ComponentValue("", "", "")),
                new Token(new Location("<unknown>", 0, 6), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 7), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 7), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithSingleDataElementWithThreeEmptyComponentDataElements() throws IOException {
        givenParserInput("UNH+:::'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.COMPOSITE_DATA, new ComponentValue("", "", "", "")),
                new Token(new Location("<unknown>", 0, 7), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 8), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 8), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithThreeDataElementsWithComponentDataElements() throws IOException {
        givenParserInput("UNH+:+:+:'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.COMPOSITE_DATA, new ComponentValue("", "")),
                new Token(new Location("<unknown>", 0, 6), TokenType.COMPOSITE_DATA, new ComponentValue("", "")),
                new Token(new Location("<unknown>", 0, 8), TokenType.COMPOSITE_DATA, new ComponentValue("", "")),
                new Token(new Location("<unknown>", 0, 9), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 10), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 10), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithSingleRepeatedDataElement() throws IOException {
        givenParserInput("UNH+*'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.REPEATED_DATA, new RepetitionValue("", "")),
                new Token(new Location("<unknown>", 0, 5), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 6), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 6), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithTwoRepeatedDataElements() throws IOException {
        givenParserInput("UNH+**'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.REPEATED_DATA, new RepetitionValue("", "", "")),
                new Token(new Location("<unknown>", 0, 6), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 7), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 7), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithThreeRepeatedDataElements() throws IOException {
        givenParserInput("UNH+***'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.REPEATED_DATA, new RepetitionValue("", "", "", "")),
                new Token(new Location("<unknown>", 0, 7), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 8), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 8), TokenType.EOF, null));
    }

    @Test
    void multipleSegmentsWithMultipleRepeatedDataElements() throws IOException {
        givenParserInput("UNH+***+***'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.REPEATED_DATA, new RepetitionValue("", "", "", "")),
                new Token(new Location("<unknown>", 0, 8), TokenType.REPEATED_DATA, new RepetitionValue("", "", "", "")),
                new Token(new Location("<unknown>", 0, 11), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 12), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 12), TokenType.EOF, null));
    }


    @Test
    void singleEmptySegmentWithSingleTagComponentValue() throws IOException {
        givenParserInput("UNH:VALUE'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new ComponentValue("UNH", "VALUE")),
                new Token(new Location("<unknown>", 0, 9), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 10), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 10), TokenType.EOF, null));
    }

    @Test
    void singleEmptySegmentWithTwoTagComponentValues() throws IOException {
        givenParserInput("UNH:VALUE1:VALUE2'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new ComponentValue("UNH", "VALUE1", "VALUE2")),
                new Token(new Location("<unknown>", 0, 17), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 18), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 18), TokenType.EOF, null));
    }

    @Test
    void singleEmptySegmentWithThreeTagComponentValues() throws IOException {
        givenParserInput("UNH:VALUE1:VALUE2:VALUE3'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new ComponentValue("UNH", "VALUE1", "VALUE2", "VALUE3")),
                new Token(new Location("<unknown>", 0, 24), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 25), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 25), TokenType.EOF, null));
    }

    @Test
    void unaPrefixedTwoEmptySegments() throws IOException {
        givenParserInput("UNA:+.? 'UNH'UNT'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 9), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 12), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 13), TokenType.BEGIN_SEGMENT, new SimpleValue("UNT")),
                new Token(new Location("<unknown>", 0, 16), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 17), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 17), TokenType.EOF, null));
    }


    @Test
    void twoEmptySegments() throws IOException {
        givenParserInput("UNH'UNT'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 3), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 4), TokenType.BEGIN_SEGMENT, new SimpleValue("UNT")),
                new Token(new Location("<unknown>", 0, 7), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 8), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 8), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithSingleSimpleValue() throws IOException {
        givenParserInput("UNH+VALUE'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.SIMPLE_DATA, new SimpleValue("VALUE")),
                new Token(new Location("<unknown>", 0, 9), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 10), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 10), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithSingleDecimalValue() throws IOException {
        givenParserInput("UNH+123.567'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.SIMPLE_DATA, new SimpleValue("123.567", null, new BigDecimal("123.567"))),
                new Token(new Location("<unknown>", 0, 11), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 12), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 12), TokenType.EOF, null));
    }

    @Test
    void twoSegmentsWithSingleSimpleValue() throws IOException {
        givenParserInput("UNH+VALUE'UNT+VALUE'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.SIMPLE_DATA, new SimpleValue("VALUE")),
                new Token(new Location("<unknown>", 0, 9), TokenType.END_SEGMENT, null),

                new Token(new Location("<unknown>", 0, 10), TokenType.BEGIN_SEGMENT, new SimpleValue("UNT")),
                new Token(new Location("<unknown>", 0, 14), TokenType.SIMPLE_DATA, new SimpleValue("VALUE")),
                new Token(new Location("<unknown>", 0, 19), TokenType.END_SEGMENT, null),

                new Token(new Location("<unknown>", 0, 20), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 20), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithSingleCompositeValue() throws IOException {
        givenParserInput("UNH+VALUE1:VALUE2'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.COMPOSITE_DATA, new ComponentValue("VALUE1", "VALUE2")),
                new Token(new Location("<unknown>", 0, 17), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 18), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 18), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithTwoCompositeValues() throws IOException {
        givenParserInput("UNH+VALUE1a:VALUE1b+VALUE2a:VALUE2b'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.COMPOSITE_DATA, new ComponentValue("VALUE1a", "VALUE1b")),
                new Token(new Location("<unknown>", 0, 20), TokenType.COMPOSITE_DATA, new ComponentValue("VALUE2a", "VALUE2b")),
                new Token(new Location("<unknown>", 0, 35), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 36), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 36), TokenType.EOF, null));
    }

    @Test
    void singleSegmentWithTwoRepeatedValues() throws IOException {
        givenParserInput("UNH+A*B*C'");

        whenParsed();

        thenTokensReadAre(
                new Token(new Location("<unknown>", 0, 0), TokenType.BEGIN_SEGMENT, new SimpleValue("UNH")),
                new Token(new Location("<unknown>", 0, 4), TokenType.REPEATED_DATA, new RepetitionValue("A", "B", "C")),
                new Token(new Location("<unknown>", 0, 9), TokenType.END_SEGMENT, null),
                new Token(new Location("<unknown>", 0, 10), TokenType.EOF, null),
                new Token(new Location("<unknown>", 0, 10), TokenType.EOF, null));
    }

    private void thenTokensReadAre(Token... tokens) {
        Assertions.assertThat(result).isEqualTo(Arrays.asList(tokens));
    }

    private void whenParsed() throws IOException {
        while (true) {
            Token next = parser.next();
            result.add(next);
            if (next.type == TokenType.EOF) {
                result.add(parser.next());
                return;
            }
        }
    }

    private void givenParserInput(String s) {
        this.parser = new TokenReader("<unknown>", new StringReader(s), Format.UNEDIFACT_DEFAULT);
    }
}
