package org.example.huffman;

import java.util.HashMap;
import java.util.Map;

/**
 * Таблица кодов Хаффмена: для каждого символа знаем его код и длину.
 */
public final class HuffmanTable {
    // Символы в том порядке, в котором к ним применялся алгоритм.
    private final byte[] symbols;
    // C[i][k] – k-й бит кода символа i (0..lengths[i)-1)
    private final boolean[][] codes;
    private final int[] lengths;

    public HuffmanTable(byte[] symbols, boolean[][] codes, int[] lengths) {
        this.symbols = symbols;
        this.codes = codes;
        this.lengths = lengths;
    }

    public int size() {
        return symbols.length;
    }

    public byte getSymbol(int index) {
        return symbols[index];
    }

    public boolean[] getCodeBits(int index) {
        return codes[index];
    }

    public int getCodeLength(int index) {
        return lengths[index];
    }

    /**
     * Построить ассоциативный массив: символ -> (массив бит).
     * Удобно для кодирования.
     */
    public Map<Byte, CodeWord> buildCodeMap() {
        Map<Byte, CodeWord> map = new HashMap<>();
        for (int i = 0; i < symbols.length; i++) {
            boolean[] bits = new boolean[lengths[i]];
            System.arraycopy(codes[i], 0, bits, 0, lengths[i]);
            map.put(symbols[i], new CodeWord(bits));
        }
        return map;
    }

    /**
     * Описание одного кодового слова.
     */
    public static final class CodeWord {
        public final boolean[] bits;

        public CodeWord(boolean[] bits) {
            this.bits = bits;
        }

        public int length() {
            return bits.length;
        }
    }
}
