package ru.ifmo.rain.vlasova.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {
    private final Path inputPath, outputPath;

    public static void main(String[] args) {
        try {
            if (args != null && args.length == 2 && args[0] != null && args[1] != null) {
                RecursiveWalk recursiveWalk = new RecursiveWalk(args[0], args[1]);
                recursiveWalk.process();
            } else {
                throw new WalkingException("Expected two arguments");
            }
        } catch (WalkingException e) {
            System.err.println(e.getMessage());
        }
    }

    private RecursiveWalk(String inputFile, String outputFile) throws WalkingException {
        try {
            inputPath = Paths.get(inputFile);
        } catch (InvalidPathException e) {
            throw new WalkingException("Can't open input file!");
        }
        try {
            outputPath = Paths.get(outputFile);
        } catch (InvalidPathException e) {
            throw new WalkingException("Can't open output file!");
        }
        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
        } catch (IOException e) {
            throw new WalkingException("Can't make parent dirs!");
        }
    }

    private void process() throws WalkingException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                String path;
                Visitor visitor = new Visitor(bufferedWriter);
                while ((path = bufferedReader.readLine()) != null) {
                    try {
                        Path currentPath = Paths.get(path);
                        Files.walkFileTree(currentPath, visitor);
                    } catch (InvalidPathException e) {
                        write(0, path, bufferedWriter);
                    }
                }
            } catch (IOException e) {
                throw new WalkingException("Can't create reader for file " + inputPath);
            }
        } catch (IOException e) {
            throw new WalkingException("Can't create writer for file " + outputPath);
        }
    }


    private FileVisitResult write(int hash, String file, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(String.format("%08x %s%n", hash, file));
        return FileVisitResult.CONTINUE;
    }

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

    public class Visitor extends SimpleFileVisitor<Path> {
        private BufferedWriter bufferedWriter;

        Visitor(BufferedWriter writer) {
            bufferedWriter = writer;
        }

        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            int hash;
            try {
                InputStream inputStream = Files.newInputStream(file);
                hash = getHash(inputStream);
            } catch (IOException e) {
                hash = 0;
            }
            return write(hash, file.toString(), bufferedWriter);
        }

        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return write(0, file.toString(), bufferedWriter);
        }
    }
}
