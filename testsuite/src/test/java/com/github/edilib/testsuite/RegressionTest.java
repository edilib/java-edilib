package com.github.edilib.testsuite;

import com.github.edilib.edifact.dom.Interchange;
import com.github.edilib.edifact.dom.InterchangeReader;
import com.github.edilib.edifact.stream.Format;
import com.github.edilib.edifact.stream.Token;
import com.github.edilib.edifact.stream.TokenReader;
import com.github.edilib.edifact.stream.TokenType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RegressionTest {

    @Test
    void readUnedifactFiles() throws IOException {
        File[] files = findFiles("unedifact");
        for (File file : files) {
            readFile(file, Format.UNEDIFACT_DEFAULT);
        }
    }

    @Test
    void readX12Files() throws IOException {
        File[] files = findFiles("x12");
        for (File file : files) {
            readFile(file, Format.X12_DEFAULT);
        }
    }

    private void readFile(File file, Format format) throws IOException {
        System.err.println("Reading " + file + "...");
        InterchangeReader reader = new InterchangeReader(file.getName(),
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8),
                format);
        Interchange interchange = reader.read();
        assertThat(interchange.getSegments()).isNotEmpty();
    }

    private File[] findFiles(String folder) throws FileNotFoundException {
        File dir = new File("src/test/resources/" + folder);
        if (!dir.isDirectory()) {
            throw new FileNotFoundException("Dir not found: " + dir.getAbsolutePath());
        }
        return dir.listFiles((dir1, name) -> name.endsWith(".txt"));
    }
}
