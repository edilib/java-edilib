package io.github.edilib.edifact.internal.scanner;

import io.github.edilib.edifact.Format;
import io.github.edilib.edifact.Location;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

// https://unece.org/fileadmin/DAM/trade/edifact/untdid/d422_s.htm
public class Scanner {

    enum State {
        INITIAL,
        IN_VALUE,
        IN_VALUE_RELEASE_SEEN
    }

    private final LookAheadReader reader;
    private final List<ScannerToken> tokens = new LinkedList<>();
    private Format format;
    private State state = State.INITIAL;

    public Scanner(String filename, Reader reader, Format format) {
        this.reader = new LookAheadReader(filename, reader);
        this.format = format;
    }

    public ScannerToken next() throws IOException {
        if (tokens.isEmpty()) {
            fill();
        }

        return tokens.remove(0);
    }

    private void fill() throws IOException {
        if (state == State.INITIAL && reader.follows("UNA")) {
            Location pos = reader.pos();
            if (!format.unaAllowed) {
                tokens.add(new ScannerToken(pos, ScannerTokenType.ERROR, null, null, null, "UNA seen but now allowed."));
            }
            String value = reader.readNChars(9);
            tokens.add(new ScannerToken(pos, ScannerTokenType.UNA_TAG, value, null, null, null));
            format = new Format(format.skipNewlineAfterSegment, true, value.charAt(3), value.charAt(4), value.charAt(5), value.charAt(6), value.charAt(7), value.charAt(8));
            return;
        }

        StringBuilder buf = new StringBuilder();
        Location pos = reader.pos();
        while (true) {
            int c = reader.peek();
            switch (state) {
                case INITIAL:
                    if (c == -1) {
                        tokens.add(new ScannerToken(pos, ScannerTokenType.EOF, null, null, null, null));
                        return;
                    } else if (c == format.releaseCharacter) {
                        reader.read();
                        state = State.IN_VALUE_RELEASE_SEEN;
                    } else if (c == format.segmentTerminator) {
                        String value = reader.readNChars(1);
                        tokens.add(new ScannerToken(pos, ScannerTokenType.SEGMENT_TERMINATOR, value, null, null, null));
                        if (format.skipNewlineAfterSegment && reader.follows("\n")) {
                            reader.readNChars(1);
                        }
                        return;
                    } else if (c == format.componentDataElementSeperator) {
                        String value = reader.readNChars(1);
                        tokens.add(new ScannerToken(pos, ScannerTokenType.COMPONENT_DATA_ELEMENT_SEPERATOR, value, null, null, null));
                        return;
                    } else if (c == format.dataElementSeperator) {
                        String value = reader.readNChars(1);
                        tokens.add(new ScannerToken(pos, ScannerTokenType.DATA_ELEMENT_SEPERATOR, value, null, null, null));
                        return;
                    } else if (format.hasRepetitionSeperator() && c == format.repetitionSeperator) {
                        String value = reader.readNChars(1);
                        tokens.add(new ScannerToken(pos, ScannerTokenType.REPETITION_SEPERATOR, value, null, null, null));
                        return;
                    } else {
                        reader.read();
                        state = State.IN_VALUE;
                        buf.append((char) c);
                    }
                    break;
                case IN_VALUE:
                    if (c == -1
                            || c == format.componentDataElementSeperator
                            || c == format.dataElementSeperator
                            || (format.hasRepetitionSeperator() && c == format.repetitionSeperator)) {
                        state = State.INITIAL;
                        String stringValue = buf.toString();
                        tokens.add(new ScannerToken(pos, ScannerTokenType.VALUE, stringValue, maybeInteger(stringValue), maybeBigDecimal(stringValue), null));
                        return;
                    } else if (c == format.segmentTerminator) {
                        state = State.INITIAL;
                        String stringValue = buf.toString();
                        tokens.add(new ScannerToken(pos, ScannerTokenType.VALUE, stringValue, maybeInteger(stringValue), maybeBigDecimal(stringValue), null));
                        if (format.skipNewlineAfterSegment && reader.follows("\n")) {
                            reader.readNChars(1);
                        }
                        return;
                    } else if (c == format.releaseCharacter) {
                        reader.read();
                        state = State.IN_VALUE_RELEASE_SEEN;
                    } else {
                        reader.read();
                        buf.append((char) c);
                    }
                    break;
                case IN_VALUE_RELEASE_SEEN:
                    if (c == -1) {
                        tokens.add(new ScannerToken(pos, ScannerTokenType.ERROR, null, null, null, "EOF after release character."));
                        return;
                    } else {
                        reader.read();
                        state = State.IN_VALUE;
                        buf.append((char) c);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown state " + state + ".");
            }
        }
    }

    private BigDecimal maybeBigDecimal(String stringValue) {
        try {
            return new BigDecimal(stringValue.replace(format.decimalMark, '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer maybeInteger(String stringValue) {
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}
