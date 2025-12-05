package org.example.huffman;

import java.io.IOException;
import java.io.InputStream;

/**
 * Обёртка над InputStream, позволяющая читать по одному биту.
 */
public class BitInputStream implements AutoCloseable {
    private final InputStream in;
    private int currentByte = 0;
    private int numBitsRemaining = 0;
    private boolean endOfStream = false;

    public BitInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * Читает один бит.
     * @return 0 или 1, либо -1 если дошли до конца потока.
     */
    public int readBit() throws IOException {
        if (endOfStream) {
            return -1;
        }

        if (numBitsRemaining == 0) {
            currentByte = in.read();
            if (currentByte == -1) {
                endOfStream = true;
                return -1;
            }
            numBitsRemaining = 8;
        }

        numBitsRemaining--;
        int bit = (currentByte >>> numBitsRemaining) & 1;
        return bit;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}

