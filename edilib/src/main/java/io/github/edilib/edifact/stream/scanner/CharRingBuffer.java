package io.github.edilib.edifact.stream.scanner;

class CharRingBuffer {
    private final char[] buffer;
    private int size = 0;
    private int indexOut = 0;
    private int indexIn = 0;

    public CharRingBuffer(int capacity) {
        buffer = new char[capacity];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public int get(int index) {
        if (index >= size) {
            return -1;
        }

        return buffer[(indexOut + index) % buffer.length];
    }

    public void add(char item) {
        if (size == buffer.length) {
            throw new IllegalStateException("Ring buffer is full.");
        }
        buffer[indexIn] = item;
        indexIn = (indexIn + 1) % buffer.length;
        size++;
    }

    public char remove() {
        if (isEmpty()) {
            throw new IllegalStateException("Ring buffer is empty.");
        }
        char item = buffer[indexOut];
        size--;
        indexOut = (indexOut + 1) % buffer.length; // wrap-around
        return item;
    }
}
