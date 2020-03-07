package ru.ifmo.rain.vlasova.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Implementor implements Impler {
    private Path getSourceRoot(Class<?> token, Path root) throws ImplerException {
        Path path = root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl.java");
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            throw new ImplerException("Can't make root dirs!");
        }
        return path;
    }

    private void validateToken(Class<?> token) throws ImplerException {
        int modifiers = token.getModifiers();
        if (token.isPrimitive() || token.isArray() || token == Enum.class || Modifier.isFinal(modifiers)
                || Modifier.isPrivate(modifiers)) {
            throw new ImplerException("Unsupported token given");
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        Path path = getSourceRoot(token, root);
        validateToken(token);
        try (BufferedWriter sourceCodeWriter = Files.newBufferedWriter(path)) {
            sourceCodeWriter.write(ImplementorCodeGenerator.generate(token));
        } catch (IOException e) {
            throw new ImplerException("Error writing into output file!");
        }
    }

    public static void main(String[] args) {
        try {
            if (args != null && args.length == 2 && args[0] != null && args[1] != null) {
                Class<?> token;
                Path root;
                try {
                    token = Class.forName(args[0]);
                    try {
                        root = Paths.get(args[1]);
                        new Implementor().implement(token, root);
                    } catch (InvalidPathException e) {
                        System.err.println("Error opening output path");
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Error getting class token");
                }
            } else {
                throw new ImplerException("Expected two arguments");
            }
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }
}
