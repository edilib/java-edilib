package io.github.edilib.edifact.stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class TokenReaderRegressionTest {
    private TokenReader parser;

    private final List<Token> result = new ArrayList<>();

    @Test
    void unedifact_invrpt1() throws IOException {
        givenParserInputFrom("unedifact_invrpt1.txt");

        whenParsed();

        Assertions.assertThat(result).hasSize(112);
    }

    @Test
    void unedifact_invrpt2() throws IOException {
        givenParserInputFrom("unedifact_invrpt2.txt");

        whenParsed();

        Assertions.assertThat(result).hasSize(73);
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

    private void givenParserInputFrom(String path) throws FileNotFoundException {

        InputStream in = getClass().getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException(path);
        }

        this.parser = new TokenReader(path, new InputStreamReader(in, StandardCharsets.UTF_8), Format.UNEDIFACT_DEFAULT);
    }
}
