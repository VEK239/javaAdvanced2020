package ru.ifmo.rain.vlasova.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Walk {
    private final Path inputPath, outputPath;

    private int getHash(InputStream inputStream) throws IOException {
        int hash = 0x811c9dc5;
        int currentBufferLength;
        byte[] buffer = new byte[4096];
        while ((currentBufferLength = inputStream.read(buffer)) != -1) {
            for (int i = 0; i < currentBufferLength; ++i) {
                hash = (hash * 0x01000193) ^ (buffer[i] & 0xff);
            }
        }
        return hash;
    }

    public Walk(String inputFile, String outputFile) throws WalkingException {
        try {
            inputPath = Paths.get(inputFile);
            outputPath = Paths.get(outputFile);
        } catch (InvalidPathException e) {
            throw new WalkingException(e.getMessage());
        }
        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
        } catch (IOException e) {
            throw new WalkingException(e.getMessage());
        }
    }

    private void process() throws WalkingException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(inputPath);
             BufferedWriter bufferedWriter = Files.newBufferedWriter(outputPath)) {
            String path;
            int hash;
            while ((path = bufferedReader.readLine()) != null) {
                try (InputStream inputStream = Files.newInputStream(Paths.get(path))) {
                    hash = getHash(inputStream);
                } catch (Exception e) {
                    hash = 0;
                }
                bufferedWriter.write(String.format("%08x", hash) + " " + path + "\n");
            }
        } catch (IOException e) {
            throw new WalkingException(e.getMessage());
        }
    }


    public static void main(String[] args) {
        try {
            if (args != null && args.length == 2 && args[0] != null && args[1] != null) {
                Walk walk = new Walk(args[0], args[1]);
                walk.process();
            } else {
                throw new WalkingException("Expected two arguments");
            }
        } catch (WalkingException e) {
            System.err.println(e.getMessage());
        }
    }
}
