package io.github.edilib.edifact;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SegmentReaderTest {

    private SegmentReader segmentReader;

    private List<Segment> segments;

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> {
            givenParserInput("", Format.UNEDIFACT_DEFAULT);

            whenSegmentsRead();
        }).isInstanceOf(IOException.class);
    }

    @Test
    void rejectsNoSegmentsAfterUnaSegment() {
        assertThatThrownBy(() -> {
            givenParserInput("UNA:+.? !", Format.UNEDIFACT_DEFAULT);

            whenSegmentsRead();
        }).isInstanceOf(IOException.class);
    }

    @Test
    void readsUnaPrefixedTagOnlySegment() throws IOException {
        givenParserInput("UNA:+.? !UNH!", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNH", emptyList()), emptyList())
        );
    }

    @Test
    void readsEmptySegment() throws IOException {
        givenParserInput("UNB'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), emptyList())
        );
    }

    @Test
    void readsTwoEmptySegments() throws IOException {
        givenParserInput("UNB'UNT'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), emptyList()),
                new Segment(new Tag("UNT", emptyList()), emptyList())
        );
    }

    @Test
    void readsSingleEmptySegmentWithSingleEmptyCompositeTag() throws IOException {
        givenParserInput("UNB:'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", asList(new SimpleValue(""))), emptyList())
        );
    }

    @Test
    void readsSingleEmptySegmentWithSingleComponentValuedCompositeTag() throws IOException {
        givenParserInput("UNB:X'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", asList(new SimpleValue("X"))), emptyList())
        );
    }

    @Test
    void readsSingleEmptySegmentWithTwoEmptyCompositeTags() throws IOException {
        givenParserInput("UNB::'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", asList(new SimpleValue(""), new SimpleValue(""))), emptyList())
        );
    }

    @Test
    void readsSingleEmptySegmentWithTwoComponentDataValuesCompositeTag() throws IOException {
        givenParserInput("UNB:A:B'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", asList(new SimpleValue("A"), new SimpleValue("B"))), emptyList())
        );
    }

    @Test
    void readsSingleSegmentWithSingleSimpleValue() throws IOException {
        givenParserInput("UNB+X'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new SimpleValue("X")))
        );
    }

    @Test
    void readsTwoSegmentWithTwoSimpleDataElements() throws IOException {
        givenParserInput("UNB+X+Y'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new SimpleValue("X"), new SimpleValue("Y")))
        );
    }

    @Test
    void readsTwoSegmentWithTwoComponentDataElements() throws IOException {
        givenParserInput("UNB+X:x+Y:y'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new ComponentValue("X", "x"), new ComponentValue("Y", "y")))
        );
    }

    @Test
    void readsTwoSegmentWithSingleRepetitionDataElement() throws IOException {
        givenParserInput("UNB+X*X2'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new RepetitionValue("X", "X2")))
        );
    }

    @Test
    void readsTwoSegmentWithTwoRepetitionDataElements() throws IOException {
        givenParserInput("UNB+X*X2+Y*Y2'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new RepetitionValue("X", "X2"), new RepetitionValue("Y", "Y2")))
        );
    }

    @Test
    void readsSingleSegmentWithSingleEmptyValue() throws IOException {
        givenParserInput("UNB+'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new SimpleValue("")))
        );
    }

    @Test
    void readsSingleSegmentWithTwoEmptyValues() throws IOException {
        givenParserInput("UNB++'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new SimpleValue(""), new SimpleValue("")))
        );
    }

    @Test
    void readsSingleSegmentWithTwoEmptyDataElementsOfEmptyComponents() throws IOException {
        givenParserInput("UNB+:+:'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new ComponentValue("", ""), new ComponentValue("", "")))
        );
    }

    @Test
    void readsSingleSegmentWithTwoEmptyDataElementsOfTwoEmptyComponents() throws IOException {
        givenParserInput("UNB+::+::'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new ComponentValue("", "", ""), new ComponentValue("", "", "")))
        );
    }

    @Test
    void readsSingleSegmentWithTwoDataElementsOfEmptyRepetitionValues() throws IOException {
        givenParserInput("UNB+*+*'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new RepetitionValue("", ""), new RepetitionValue("", "")))
        );
    }


    @Test
    void readsSingleSegmentWithTwoDataElementsOfTwoRepetitionValues() throws IOException {
        givenParserInput("UNB+**+**'", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).containsExactly(
                new Segment(new Tag("UNB", emptyList()), asList(new RepetitionValue("", "", ""), new RepetitionValue("", "", "")))
        );
    }

    @Test
    void detectsMissingSegmentTerminator() {
        assertThatThrownBy(() -> {
            givenParserInput("ABC", Format.UNEDIFACT_DEFAULT);

            whenSegmentsRead();
        }).isInstanceOf(IOException.class);
    }

    @Test
    void detectsRepetitionSeperatorInTag() {
        assertThatThrownBy(() -> {
            givenParserInput("ABC*'", Format.UNEDIFACT_DEFAULT);

            whenSegmentsRead();
        }).isInstanceOf(IOException.class);
    }


    @Test
    void rejectsMixedComponentAfterRepetitionDataElements() {
        assertThatThrownBy(() -> {
            givenParserInput("ABC+*:'", Format.UNEDIFACT_DEFAULT);

            whenSegmentsRead();
        }).isInstanceOf(IOException.class);
    }

    @Test
    void rejectsMixedRepetitionAfterComponentDataElements() {
        assertThatThrownBy(() -> {
            givenParserInput("ABC+:*'", Format.UNEDIFACT_DEFAULT);

            whenSegmentsRead();
        }).isInstanceOf(IOException.class);
    }

    private void givenParserInput(String data, Format format) {
        segmentReader = new SegmentReader("<unknown>", new StringReader(data), format);
    }

    private void whenSegmentsRead() throws IOException {
        segments = segmentReader.readAll();
    }
}