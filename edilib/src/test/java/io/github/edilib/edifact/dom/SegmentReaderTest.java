package io.github.edilib.edifact.dom;

import io.github.edilib.edifact.stream.Format;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SegmentReaderTest {

    private SegmentReader segmentReader;

    private List<Segment> segments;

    @Test
    void unedifact_invrpt1() throws IOException {
        givenParserInputFrom("unedifact_invrpt1.txt", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).hasSize(26);
    }

    @Test
    void unedifact_invrpt2() throws IOException {
        givenParserInputFrom("unedifact_invrpt2.txt", Format.UNEDIFACT_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).hasSize(16);
    }

    @Test
    void x12_purchase_order1() throws IOException {
        givenParserInputFrom("x12_purchase_order1.txt", Format.X12_DEFAULT);

        whenSegmentsRead();

        assertThat(segments).hasSize(39);
    }

    private void whenSegmentsRead() throws IOException {
        segments = segmentReader.readAll();
    }

    private void givenParserInputFrom(String path, Format format) throws FileNotFoundException {

        InputStream in = getClass().getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException(path);
        }

        segmentReader = new SegmentReader(path, new InputStreamReader(in, StandardCharsets.UTF_8), format);
    }
}