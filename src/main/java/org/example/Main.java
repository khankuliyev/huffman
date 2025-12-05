package org.example;

import org.example.huffman.HuffmanCodec;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java -jar huffman.jar <encode|decode> <input> <output>");
            return;
        }

        String command = args[0];
        Path input = Path.of(args[1]);
        Path output = Path.of(args[2]);

        HuffmanCodec codec = new HuffmanCodec();

        try {
            if (command.equalsIgnoreCase("encode")) {
                codec.encode(input, output);
                System.out.println("File encoded successfully!");
            } else if (command.equalsIgnoreCase("decode")) {
                codec.decode(input, output);
                System.out.println("File decoded successfully!");
            } else {
                System.out.println("Unknown command: " + command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

