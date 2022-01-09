package io.github.edilib.edifact.dom;

import io.github.edilib.edifact.stream.Format;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class InterchangeReaderTest {

    private InterchangeReader interchangeReader;

    private Interchange interchange;

    @Test
    void unedifact_invrpt1() throws IOException {
        givenParserInputFrom("unedifact_invrpt1.txt", Format.UNEDIFACT_DEFAULT);

        whenInterchangeRead();

        assertThat(interchange.getSegments()).hasSize(26);
    }

    @Test
    void unedifact_invrpt2() throws IOException {
        givenParserInputFrom("unedifact_invrpt2.txt", Format.UNEDIFACT_DEFAULT);

        whenInterchangeRead();

        assertThat(interchange.getSegments()).hasSize(16);
    }

    @Test
    void x12_purchase_order1() throws IOException {
        givenParserInputFrom("x12_purchase_order1.txt", Format.X12_DEFAULT);

        whenInterchangeRead();

        assertThat(interchange.getSegments()).hasSize(39);
    }

    private void whenInterchangeRead() throws IOException {
        interchange = interchangeReader.read();
    }

    private void givenParserInputFrom(String path, Format format) throws FileNotFoundException {

        InputStream in = getClass().getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException(path);
        }

        interchangeReader = new InterchangeReader(path, new InputStreamReader(in, StandardCharsets.UTF_8), format);
    }
}