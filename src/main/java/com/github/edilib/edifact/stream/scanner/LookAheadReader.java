package com.github.edilib.edifact.stream.scanner;

import com.github.edilib.edifact.stream.Location;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

class LookAheadReader implements AutoCloseable {

    private final Reader reader;
    private final CharRingBuffer charRingBuffer = new CharRingBuffer(128);
    private int pos = 0;
    private final String filename;

    public LookAheadReader(String filename, Reader rd) {
        this.filename = filename;
        this.reader = rd;
    }

    public String readNChars(int n) throws IOException {
        fill(n);

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < n; ++i) {
            int c = read();
            if (c == -1) {
                throw new EOFException("Expected " + n + " chars, but got " + buf + " and EOF.");
            }

            buf.append((char) c);
        }

        return buf.toString();
    }

    public boolean follows(String s) throws IOException {
        fill(s.length());

        if (charRingBuffer.size() < s.length()) {
            return false;
        }

        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) != charRingBuffer.get(i)) {
                return false;
            }
        }

        return true;
    }

    public int peek() throws IOException {
        fill(1);

        if (charRingBuffer.size() < 1) {
            return -1;
        }

        return charRingBuffer.get(0);
    }

    public int read() throws IOException {
        fill(1);

        if (this.charRingBuffer.isEmpty()) {
            return -1;
        }

        pos++;

        return this.charRingBuffer.remove();
    }

    private void fill(int n) throws IOException {
        while (charRingBuffer.size() < n) {
            int c = this.reader.read();
            if (c == -1) {
                break;
            }
            charRingBuffer.add((char) c);
        }
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    public Location pos() {
        return new Location(this.filename, 0, pos);
    }
}
