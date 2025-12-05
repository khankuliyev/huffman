package org.example.huffman;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HuffmanCodec {

    private static final byte[] MAGIC = {'H', 'F', 'M'};
    private static final byte VERSION = 1;

    // encode

    public void encode(Path input, Path output) throws IOException {

        // 1. Чтение всех байтов входного файла
        byte[] data = Files.readAllBytes(input);

        // 2. Подсчёт частот
        Map<Byte, Long> freqMap = new HashMap<>();
        for (byte b : data) {
            freqMap.put(b, freqMap.getOrDefault(b, 0L) + 1);
        }

        // 3. Построение таблицы Хаффмена
        HuffmanTable table = HuffmanBuilder.buildFromFrequencies(freqMap);
        Map<Byte, HuffmanTable.CodeWord> codeMap = table.buildCodeMap();

        // 4. Запись выходного файла
        try (OutputStream fos = Files.newOutputStream(output);
             BitOutputStream bos = new BitOutputStream(fos)) {

            // 4.1 MAGIC + версия
            fos.write(MAGIC);
            fos.write(VERSION);

            // 4.2 Количество исходных байтов
            writeLong(fos, data.length);

            // 4.3 Количество различных символов
            writeInt(fos, table.size());

            // 4.4 Запись словаря (символ + частота)
            for (int i = 0; i < table.size(); i++) {
                byte symbol = table.getSymbol(i);
                long freq = freqMap.get(symbol);
                fos.write(symbol);
                writeLong(fos, freq);
            }

            // 5. Запись самих закодированных данных
            for (byte b : data) {
                HuffmanTable.CodeWord cw = codeMap.get(b);
                bos.writeBits(cw.bits, cw.length());
            }
        }
    }

    // decode
    public void decode(Path input, Path output) throws IOException {

        try (InputStream fis = Files.newInputStream(input);
             BitInputStream bis = new BitInputStream(fis);
             OutputStream out = Files.newOutputStream(output)) {

            // 1. Проверка MAGIC
            if (fis.read() != MAGIC[0] || fis.read() != MAGIC[1] || fis.read() != MAGIC[2]) {
                throw new IOException("Not a Huffman file");
            }

            // 2. Версия
            int version = fis.read();
            if (version != VERSION) {
                throw new IOException("Unsupported Huffman file version");
            }

            // 3. Длина исходного файла
            long originalSize = readLong(fis);

            // 4. Количество символов
            int dictSize = readInt(fis);

            // 5. Чтение словаря
            Map<Byte, Long> freqMap = new HashMap<>();
            for (int i = 0; i < dictSize; i++) {
                byte symbol = (byte) fis.read();
                long freq = readLong(fis);
                freqMap.put(symbol, freq);
            }

            // 6. Восстановление таблицы Хаффмена
            HuffmanTable table = HuffmanBuilder.buildFromFrequencies(freqMap);

            // Чтобы быстро искать символ по битовой последовательности:
            // построим "дерево" вручную
            DecodeNode root = buildDecodeTree(table);

            // 7. Декодирование
            DecodeNode node = root;
            long written = 0;

            while (written < originalSize) {
                int bit = bis.readBit();
                if (bit == -1) break;

                node = bit == 0 ? node.zero : node.one;

                if (node.isLeaf()) {
                    out.write(node.symbol);
                    written++;
                    node = root;
                }
            }
        }
    }

    // decode tree

    private static class DecodeNode {
        byte symbol;
        DecodeNode zero, one;

        boolean isLeaf() {
            return zero == null && one == null;
        }
    }

    private DecodeNode buildDecodeTree(HuffmanTable table) {
        DecodeNode root = new DecodeNode();

        for (int i = 0; i < table.size(); i++) {
            byte symbol = table.getSymbol(i);
            boolean[] bits = table.getCodeBits(i);
            int len = table.getCodeLength(i);

            DecodeNode node = root;
            for (int k = 0; k < len; k++) {
                if (bits[k]) {
                    if (node.one == null) node.one = new DecodeNode();
                    node = node.one;
                } else {
                    if (node.zero == null) node.zero = new DecodeNode();
                    node = node.zero;
                }
            }

            node.symbol = symbol;
        }

        return root;
    }

    // utils

    private void writeInt(OutputStream os, int value) throws IOException {
        os.write((value >>> 24) & 0xFF);
        os.write((value >>> 16) & 0xFF);
        os.write((value >>> 8) & 0xFF);
        os.write(value & 0xFF);
    }

    private int readInt(InputStream is) throws IOException {
        return (is.read() << 24) |
                (is.read() << 16) |
                (is.read() << 8) |
                is.read();
    }

    private void writeLong(OutputStream os, long value) throws IOException {
        for (int i = 7; i >= 0; i--) {
            os.write((int)((value >>> (8 * i)) & 0xFF));
        }
    }

    private long readLong(InputStream is) throws IOException {
        long value = 0;
        for (int i = 7; i >= 0; i--) {
            value |= ((long)is.read() & 0xFF) << (8 * i);
        }
        return value;
    }
}

