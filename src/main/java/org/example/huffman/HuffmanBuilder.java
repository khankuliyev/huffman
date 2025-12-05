package org.example.huffman;

import java.util.*;

/**
 * Построение кодов Хаффмена. Пользовался псевдокодом из материалов по дисциплине
 * "Дискретная математика" (процедуры Huffman, Up, Down).
 */
public final class HuffmanBuilder {

    /**
     * Вспомогательная структура: символ и его частота.
     */
    private static final class SymbolFreq {
        final byte symbol;
        final long freq;

        SymbolFreq(byte symbol, long freq) {
            this.symbol = symbol;
            this.freq = freq;
        }
    }

    /**
     * Построить таблицу кодов Хаффмена по частотам.
     * freqMap: символ -> количество появлений (частота > 0).
     */
    public static HuffmanTable buildFromFrequencies(Map<Byte, Long> freqMap) {
        if (freqMap.isEmpty()) {
            throw new IllegalArgumentException("freqMap is empty");
        }

        // Собираем и сортируем символы по невозрастанию частоты (как в псевдокоде: P упорядочен по невозрастанию)
        List<SymbolFreq> list = new ArrayList<>();
        for (Map.Entry<Byte, Long> e : freqMap.entrySet()) {
            long f = e.getValue();
            if (f > 0) {
                list.add(new SymbolFreq(e.getKey(), f));
            }
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("All frequencies are zero");
        }

        // Сортировка: по частоте (убывание), при равной частоте – по самому байту (возрастание)
        list.sort((a, b) -> {
            int cmp = Long.compare(b.freq, a.freq); // по убыванию
            if (cmp != 0) return cmp;
            int aa = a.symbol & 0xFF;
            int bb = b.symbol & 0xFF;
            return Integer.compare(aa, bb);
        });

        int n = list.size();
        byte[] symbols = new byte[n];
        long[] P = new long[n];     // массив вероятностей/частот
        for (int i = 0; i < n; i++) {
            symbols[i] = list.get(i).symbol;
            P[i] = list.get(i).freq;
        }

        // Максимальная длина кода в дереве Хаффмена не превосходит n-1
        int maxLen = Math.max(1, n - 1);
        boolean[][] C = new boolean[n][maxLen]; // C[i][k] – k-й бит кода i
        int[] L = new int[n];                   // длины кодов

        if (n == 1) {
            // Особый случай: один символ -> код "0" длины 1
            C[0][0] = false;
            L[0] = 1;
        } else {
            HuffmanBuilder builder = new HuffmanBuilder(P, C, L);
            builder.huffman(n);
        }

        return new HuffmanTable(symbols, C, L);
    }

    // реализация псевдокода

    private final long[] P;       // массив частот (как P в конспекте)
    private final boolean[][] C; // массив кодов
    private final int[] L;       // длины кодов

    private HuffmanBuilder(long[] P, boolean[][] C, int[] L) {
        this.P = P;
        this.C = C;
        this.L = L;
    }

    /**
     * Основная рекурсивная процедура Huffman(n).
     * n – количество "букв" (символов) в текущей задаче.
     */
    private void huffman(int n) {
        if (n == 2) {
            // База рекурсии: двум буквам назначаются коды 0 и 1.
            L[0] = 1;
            C[0][0] = false; // код 0

            L[1] = 1;
            C[1][0] = true;  // код 1
        } else {
            // q := P[n-1] + P[n] в 1-индексации
            long q = P[n - 2] + P[n - 1];

            // j := Up(n, q)  (в нашей реализации Up работает с первыми n-1 элементами массива)
            int j = up(n - 1, q);

            // Рекурсивный вызов Huffman(n - 1)
            huffman(n - 1);

            // Down(n, j) – достраиваем коды
            down(n, j);
        }
    }

    /**
     * Процедура Up: вставка суммы q в отсортированный по невозрастанию массив P
     * (используются только первые len элементов массива).
     */
    private int up(int len, long q) {
        int j = 0;
        int i;
        for (i = len - 1; i >= 1; i--) {
            if (P[i - 1] < q) {
                // сдвиг элемента массива
                P[i] = P[i - 1];
            } else {
                j = i;
                break;
            }
        }
        if (i == 0) {
            j = 0;
        }
        P[j] = q;
        return j;
    }

    /**
     * Процедура Down.
     * На входе: оптимальные коды для n-1 букв (в первых n-1 элементах C и L),
     * нужно получить коды для n букв (в первых n элементах C и L).
     *
     * n – длина обрабатываемой части массива P,
     * j – номер "разделяемой" буквы.
     */
    private void down(int n, int j) {
        // c := C[j,*], l := ℓ[j]
        boolean[] c = C[j].clone();
        int l = L[j];

        // for i from j to n-2 do
        //   C[i,*] := C[i+1,*]
        //   ℓ[i]   := ℓ[i+1]
        // end for
        for (int i = j; i <= n - 2; i++) {
            C[i] = C[i + 1].clone();
            L[i] = L[i + 1];
        }

        // C[n-1,*] := c; C[n,*] := c    (1-индексация)
        // -> C[n-2] и C[n-1] в 0-индексации
        C[n - 2] = c.clone();
        C[n - 1] = c.clone();

        // C[n-1, l+1] := 0; C[n, l+1] := 1
        C[n - 2][l] = false;
        C[n - 1][l] = true;

        // ℓ[n-1] := l+1; ℓ[n] := l+1
        L[n - 2] = l + 1;
        L[n - 1] = l + 1;
    }
}
