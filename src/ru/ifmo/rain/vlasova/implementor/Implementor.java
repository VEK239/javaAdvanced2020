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

/**
 * Class implementing {@link Impler}, {@link JarImpler}. Provides methods to generate classes and interfaces.
 *
 * @author Elizaveta Vlasova (vlasova.elizaveta@yandex.ru)
 */
public class Implementor implements Impler, JarImpler {
    /**
     * Default constructor
     */
    public Implementor() {
    }

    /**
     * Changes the given path into a correct one. Replacing . with correct separator, appends Impl.java.
     *
     * @param root  the given path
     * @param token the {@link Class} token of implemented class
     * @return the correct path
     */
    private Path getFullPath(Path root, Class<?> token) {
        return root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl.java");
    }

    /**
     * Creates all parent directories of a given root.
     *
     * @param token the {@link Class} token of implemented class
     * @param root  the given path
     * @return the encoded string
     * @throws ImplerException if an error occurs while creating parent dirs
     */
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


    /**
     * Validates the given {@link Class} token. Not supported {@link Class} tokens include arrays, primitives, enums,
     * final or private modifiers
     *
     * @param token the {@link Class} token of implemented class
     * @throws ImplerException if an unsupported token given
     */
    private void validateToken(Class<?> token) throws ImplerException {
        int modifiers = token.getModifiers();
        if (token.isPrimitive() || token.isArray() || token == Enum.class || Modifier.isFinal(modifiers)
                || Modifier.isPrivate(modifiers)) {
            throw new ImplerException("Unsupported token given");
        }
    }


    /**
     * Encodes the provided string.
     *
     * @param s a string to encode
     * @return the encoded string
     */
    private String encode(String s) {
        StringBuilder builder = new StringBuilder();
        for (char c : s.toCharArray()) {
            builder.append("\\u").append(String.format("%04x", (int) c));
        }
        return builder.toString();
    }

    /**
     * Implements the given class.
     * Invokes {@link ImplementorCodeGenerator#generate(Class)} to generate the code of the implementing class
     *
     * @param token the {@link Class} token of implemented class
     * @param root  the directory for locating the implementation
     * @throws ImplerException if an error occurs while writing the result into the output file
     * @see ImplementorCodeGenerator#generate(Class)
     */
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


    /**
     * Invoked in case the program was run with -jar key.
     * Creates the temporary directory and invokes methods for implementing, compiling, creating jar file.
     *
     * @param token   the {@link Class} token of implemented class
     * @param jarFile name of jar file to be generated
     * @throws ImplerException if an error occurs while creating parent path for jar or temporary directory for source
     * @see Implementor#createJar(Class, Path, Path)
     * @see Implementor#compile(Class, Path)
     * @see Implementor#implement(Class, Path)
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path parentPath = jarFile.toAbsolutePath().normalize().getParent();
        System.out.println(parentPath.toString());
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


    /**
     * Compiles the solution.
     *
     * @param token      the {@link Class} token of implemented class
     * @param sourcePath the temp directory with classes
     * @throws ImplerException if an error occurs while compiling the result or searching for classpath
     */
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
        String[] args = {"-encoding", "UTF-8", "-cp",
                sourcePath.toString() + File.pathSeparator + tokenClassPath.toString(),
                getFullPath(sourcePath, token).toString()};
        if (compiler == null || compiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Error compiling classes!");
        }
    }

    /**
     * Creates the jar file for the solution.
     *
     * @param token      the {@link Class} token of implemented class
     * @param sourcePath the temp directory with classes
     * @param jarFile    name of jar file to be generated
     * @throws ImplerException if an error occurs while creating jar file
     */
    private void createJar(Class<?> token, Path sourcePath, Path jarFile) throws ImplerException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        String localName = token.getPackageName().replace(".", "/") + "/" +
                token.getSimpleName() + "Impl.class";
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            out.putNextEntry(new ZipEntry(localName));
            System.out.println(sourcePath.resolve(localName));
            Files.copy(sourcePath.resolve(localName), out);
        } catch (IOException e) {
            throw new ImplerException("Error creating jar file", e);
        }
    }


    /**
     * Main method that provides console interface.
     * Usage: ([-jar]) [ClassName] [Path]
     *
     * @param args arguments entered in command line
     * @see Implementor#implement(Class, Path)
     * @see Implementor#implementJar(Class, Path)
     */
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
                    System.out.println(args[jarDelta]);
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
