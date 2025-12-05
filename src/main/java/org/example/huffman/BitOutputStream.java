package org.example.huffman;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Обёртка над OutputStream, позволяющая записывать по одному биту.
 */
public class BitOutputStream implements AutoCloseable {
    private final OutputStream out;
    private int currentByte = 0;    // накапливаем биты здесь
    private int numBitsFilled = 0;  // сколько бит уже записано в currentByte

    public BitOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * Записать один бит (0 или 1).
     */
    public void writeBit(int bit) throws IOException {
        if (bit != 0 && bit != 1) {
            throw new IllegalArgumentException("bit must be 0 or 1");
        }

        // Сдвигаем влево и добавляем бит
        currentByte = (currentByte << 1) | bit;
        numBitsFilled++;

        // Если набрали 8 бит — сбрасываем байт в поток
        if (numBitsFilled == 8) {
            flushCurrentByte();
        }
    }

    /**
     * Записать несколько бит из массива boolean.
     */
    public void writeBits(boolean[] bits, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            writeBit(bits[i] ? 1 : 0);
        }
    }

    /**
     * Сбрасываем незаполненный байт (если есть),
     * дополняя его нулями справа.
     */
    private void flushCurrentByte() throws IOException {
        if (numBitsFilled > 0) {
            // Сдвигаем оставшиеся биты в старшие разряды
            currentByte <<= (8 - numBitsFilled);
            out.write(currentByte);
            currentByte = 0;
            numBitsFilled = 0;
        }
    }

    /**
     * Закрыть поток: сначала дописать незавершённый байт, потом закрыть out.
     */
    @Override
    public void close() throws IOException {
        flushCurrentByte();
        out.close();
    }
}

