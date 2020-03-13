package ru.ifmo.rain.vlasova.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Implementor implements Impler, JarImpler {
    private Path getFullPath(Path root, Class<?> token) {
        return root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl.java");
    }

    private Path getSourceRoot(Class<?> token, Path root) throws ImplerException {
        Path path = getFullPath(root, token);
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

    private String encode(String s) {
        StringBuilder sb = new StringBuilder();
        char[] charArray = s.toCharArray();
        for (char c : charArray) {
            if (c < 128) {
                sb.append(c);
            } else {
                sb.append("\\u").append(String.format("%04x", (int) c));
            }
        }
        return sb.toString();
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        Path path = getSourceRoot(token, root);
        validateToken(token);
        try (BufferedWriter sourceCodeWriter = Files.newBufferedWriter(path)) {
            sourceCodeWriter.write(encode(ImplementorCodeGenerator.generate(token)));
        } catch (IOException e) {
            throw new ImplerException("Error writing into output file!");
        }
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path parentPath = jarFile.toAbsolutePath().normalize().getParent();
        try {
            Files.createDirectories(parentPath);
        } catch (IOException e) {
            throw new ImplerException("Can't create parent path for jar file", e);
        }
        Path sourceDir;
        try {
            sourceDir = Files.createTempDirectory(parentPath, "tmp");
        } catch (IOException e) {
            throw new ImplerException("Can't create temp directory", e);
        }
        implement(token, sourceDir);
        compile(token, sourceDir);
        createJar(token, sourceDir, jarFile);
    }

    private void compile(Class<?> token, Path sourcePath) throws ImplerException {
        Path tokenClassPath;
        try {
            CodeSource codeSource = token.getProtectionDomain().getCodeSource();
            URL url = codeSource.getLocation();
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            tokenClassPath = Path.of(path);
        } catch (InvalidPathException e) {
            throw new ImplerException("Cannot find classpath", e);
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] args = {"-cp",
                sourcePath.toString() + File.pathSeparator + tokenClassPath.toString(),
                getFullPath(sourcePath, token).toString()};
        if (compiler == null || compiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Error compiling classes!");
        }
    }


    private void createJar(Class<?> token, Path sourcePath, Path jarFile) throws ImplerException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        String localName = token.getPackageName().replace(".", "/") + "/" +
                token.getSimpleName() + "Impl.class";
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            out.putNextEntry(new ZipEntry(localName));
            Files.copy(sourcePath.resolve(localName), out);
        } catch (IOException e) {
            throw new ImplerException("Error creating jar file", e);
        }
    }

    public static void main(String[] args) {
        try {
            if (args != null && args.length >= 2 && args.length <= 3 && args[0] != null && args[1] != null &&
                    (args.length != 3 || args[2] != null)) {
                Class<?> token;
                Path root;
                int jarDelta = 0;
                if (args.length == 3) {
                    if (!args[0].equals("-jar")) {
                        throw new ImplerException("'-jar' should go first if three arguments!");
                    }
                    jarDelta = 1;
                }
                try {
                    token = Class.forName(args[jarDelta]);
                    try {
                        root = Paths.get(args[1 + jarDelta]);
                        if (jarDelta == 0) {
                            new Implementor().implement(token, root);
                        } else {
                            new Implementor().implementJar(token, root);
                        }
                    } catch (InvalidPathException e) {
                        System.err.println("Error opening output path");
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Error getting class token");
                }
            } else {
                throw new ImplerException("Expected two or three arguments");
            }
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }
}
