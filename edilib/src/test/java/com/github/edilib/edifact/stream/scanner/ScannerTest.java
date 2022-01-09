package com.github.edilib.edifact.stream.scanner;

import com.github.edilib.edifact.stream.Format;
import com.github.edilib.edifact.stream.Location;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScannerTest {

    private Scanner scanner;
    private List<ScannerToken> result;

    @Test
    void scansValue() throws IOException {
        givenScannrInput("GHT");

        whenAllRead();

        thenTokensReadAre(new ScannerToken(new Location("<unknown>", 0, 0), ScannerTokenType.VALUE, "GHT", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 3), ScannerTokenType.EOF, null, null, null, null));
    }

    @Test
    void scansEOF() throws IOException {
        givenScannrInput("");

        whenAllRead();

        thenTokensReadAre(new ScannerToken(new Location("<unknown>", 0, 0), ScannerTokenType.EOF, null, null, null, null));
    }

    @Test
    void scansEscapedReleaseChar() throws IOException {
        givenScannrInput("??");

        whenAllRead();

        thenTokensReadAre(new ScannerToken(new Location("<unknown>", 0, 0), ScannerTokenType.VALUE, "?", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 2), ScannerTokenType.EOF, null, null, null, null));
    }

    @Test
    void scansDecimal() throws IOException {
        givenScannrInput("UNA:+,? !!ABC+1234,567!");

        whenAllRead();

        thenTokensReadAre(new ScannerToken(new Location("<unknown>", 0, 0), ScannerTokenType.UNA_TAG, "UNA:+,? !", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 9), ScannerTokenType.SEGMENT_TERMINATOR, "!", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 10), ScannerTokenType.VALUE, "ABC", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 13), ScannerTokenType.DATA_ELEMENT_SEPERATOR, "+", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 14), ScannerTokenType.VALUE, "1234,567", null, new BigDecimal("1234.567"), null),
                new ScannerToken(new Location("<unknown>", 0, 22), ScannerTokenType.SEGMENT_TERMINATOR, "!", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 23), ScannerTokenType.EOF, null, null, null, null));
    }

    @Test
    void scansInteger() throws IOException {
        givenScannrInput("UNA:+,? !!ABC+12345678!");

        whenAllRead();

        thenTokensReadAre(new ScannerToken(new Location("<unknown>", 0, 0), ScannerTokenType.UNA_TAG, "UNA:+,? !", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 9), ScannerTokenType.SEGMENT_TERMINATOR, "!", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 10), ScannerTokenType.VALUE, "ABC", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 13), ScannerTokenType.DATA_ELEMENT_SEPERATOR, "+", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 14), ScannerTokenType.VALUE, "12345678", 12345678, new BigDecimal("12345678"), null),
                new ScannerToken(new Location("<unknown>", 0, 22), ScannerTokenType.SEGMENT_TERMINATOR, "!", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 23), ScannerTokenType.EOF, null, null, null, null));
    }

    @Test
    void scansEscapedSegmentTerminatorChar() throws IOException {
        givenScannrInput("?+");

        whenAllRead();

        thenTokensReadAre(new ScannerToken(new Location("<unknown>", 0, 0), ScannerTokenType.VALUE, "+", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 2), ScannerTokenType.EOF, null, null, null, null));
    }

    @Test
    void scansUNASegmentAndAdapts() throws IOException {
        givenScannrInput("UNA:+.? !!");

        whenAllRead();

        thenTokensReadAre(new ScannerToken(new Location("<unknown>", 0, 0), ScannerTokenType.UNA_TAG, "UNA:+.? !", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 9), ScannerTokenType.SEGMENT_TERMINATOR, "!", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 10), ScannerTokenType.EOF, null, null, null, null));
    }

    @Test
    void scansDefaultSegmentTerminator() throws IOException {
        givenScannrInput("'");

        whenAllRead();

        thenTokensReadAre(new ScannerToken(new Location("<unknown>", 0, 0), ScannerTokenType.SEGMENT_TERMINATOR, "'", null, null, null),
                new ScannerToken(new Location("<unknown>", 0, 1), ScannerTokenType.EOF, null, null, null, null));
    }

    private void thenTokensReadAre(ScannerToken... tokens) {
        Assertions.assertThat(result).isEqualTo(Arrays.asList(tokens));
    }

    private void whenAllRead() throws IOException {
        result = new ArrayList<>();
        while (true) {
            ScannerToken next = scanner.next();
            result.add(next);
            if (next.type == ScannerTokenType.EOF) {
                break;
            }
        }
    }

    private void givenScannrInput(String input) {
        scanner = new Scanner("<unknown>", new StringReader(input), Format.UNEDIFACT_DEFAULT);
    }
}