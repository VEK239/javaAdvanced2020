package ru.ifmo.rain.vlasova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class that provides methods to generate classes and interfaces source code.
 *
 * @author Elizaveta Vlasova (vlasova.elizaveta@yandex.ru)
 */
class ImplementorCodeGenerator {

    /**
     * An open brace constant.
     */
    private static final String OPEN_BRACE = "(";

    /**
     * A closing brace constant.
     */
    private static final String CLOSE_BRACE = ")";

    /**
     * A closing block constant.
     */
    private static final String END_OF_EXEC = "}";

    /**
     * An opening block constant.
     */
    private static final String START_OF_EXEC = "{";

    /**
     * A space constant.
     */
    private static final String SPACE = " ";

    /**
     * An end of expression constant.
     */
    private static final String END_OF_EXP = ";";

    /**
     * A system defined line separator constant.
     */
    private static final String NEW_LINE = System.lineSeparator();

    /**
     * An empty string constant.
     */
    private static final String EMPTY = "";

    /**
     * A tabulation constant.
     */
    private static final String TAB = "\t";


    /**
     * An inner class that provides unique names for methods arguments
     */
    private static class VariableCounter implements Supplier<String> {
        /**
         * An id of the maximum used number for current instance.
         */
        int id = 0;

        /**
         * Returns a new variable name.
         *
         * @return the unique variable name
         */
        public String get() {
            return "var" + id++;
        }
    }

    /**
     * Returns the default value for a given {@link Class} token.
     *
     * @param clazz the given {@link Class} token
     * @return the default value for given {@link Class} token
     */
    private static String getDefaultValue(Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return "null";
        } else if (clazz.equals(void.class)) {
            return EMPTY;
        } else if (clazz.equals(boolean.class)) {
            return "false";
        } else {
            return "0";
        }
    }

    /**
     * Concatenates the given list of strings by a separator string.
     *
     * @param delimiter the string for the args to be separated by
     * @param args      a list of strings to be returned as one
     * @return the resulting string consisting of the given strings separated by the delimiter
     */
    private static String concatenate(String delimiter, String... args) {
        return String.join(delimiter, args);
    }

    /**
     * Returns the packages of a given {@link Class} token.
     *
     * @param token the given {@link Class} token
     * @return a string of the packages of a given {@link Class} token
     */
    private static String generatePackages(Class<?> token) {
        Package pack = token.getPackage();
        return (pack == null ? "" : concatenate(SPACE, "package", pack.getName(), END_OF_EXP, NEW_LINE));
    }

    /**
     * Returns the new class name for a given {@link Class} token.
     *
     * @param token the given {@link Class} token
     * @return the new class name
     */
    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Generates the opening line for the given {@link Class} token.
     *
     * @param token the given {@link Class} token
     * @return the opening line of the class
     */
    private static String generateClassOpeningLine(Class<?> token) {
        return concatenate(SPACE,
                Modifier.toString(token.getModifiers() & ~Modifier.INTERFACE & ~Modifier.ABSTRACT
                        & ~Modifier.PROTECTED & ~Modifier.STATIC),
                "class",
                getClassName(token),
                (token.isInterface() ? "implements" : "extends"),
                token.getCanonicalName(),
                START_OF_EXEC);
    }

    /**
     * Generates the opening line for the given executable (method or constructor).
     *
     * @param executable the given {@link Class} token
     * @param name       the given {@link Class} token
     * @return the opening line of the executable
     */
    private static String generateExecutableOpeningLine(Executable executable, String name) {
        return concatenate(SPACE,
                Modifier.toString(executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT),
                name,
                OPEN_BRACE + getArguments(executable, true) + CLOSE_BRACE,
                getThrownExceptions(executable),
                START_OF_EXEC);
    }

    /**
     * Generates the method source code.
     *
     * @param method the given method
     * @return the source code of the method
     */
    private static String generateMethod(Method method) {
        return concatenate(NEW_LINE,
                TAB + generateExecutableOpeningLine(method,
                        concatenate(SPACE, method.getReturnType().getCanonicalName(), method.getName())),
                concatenate(SPACE, TAB + TAB + "return", getDefaultValue(method.getReturnType())) + END_OF_EXP,
                TAB + END_OF_EXEC);
    }

    /**
     * Generates the constructor source code.
     *
     * @param constructor the given constructor
     * @return the source code of the constructor
     */
    private static String generateConstructor(Constructor constructor) {
        return concatenate(NEW_LINE,
                TAB + generateExecutableOpeningLine(constructor,
                        getClassName(constructor.getDeclaringClass())),
                concatenate(EMPTY, TAB + TAB + "super", OPEN_BRACE, getArguments(constructor, false), CLOSE_BRACE) +
                        END_OF_EXP,
                TAB + END_OF_EXEC);
    }

    /**
     * Generates the string of the thrown exceptions of the executable.
     *
     * @param executable the given executable (constructor or method)
     * @return the string with thrown exceptions
     */
    private static String getThrownExceptions(Executable executable) {
        Class[] exceptions = executable.getExceptionTypes();
        if (exceptions.length != 0) {
            return concatenate(SPACE, "throws",
                    Arrays.stream(exceptions).map(Class::getCanonicalName).collect(Collectors.joining(", ")));
        }
        return "";
    }

    /**
     * Generates the string with arguments for the given executable.
     *
     * @param executable    the given executable (constructor or method)
     * @param isDeclaration true if declaring the arguments, false if calling a function
     * @return the string of arguments
     */
    private static String getArguments(Executable executable, boolean isDeclaration) {
        VariableCounter counter = new VariableCounter();
        return Arrays.stream(executable.getParameterTypes())
                .map(c -> ((isDeclaration ? c.getCanonicalName() + SPACE : EMPTY) + counter.get()))
                .collect(Collectors.joining(", "));
    }

    /**
     * Generates a set of {@link ComparedMethod} from the given array of methods.
     *
     * @param methods the given methods
     * @return the set of {@link ComparedMethod}
     * @see ComparedMethod
     */
    private static Set<ComparedMethod> getComparedMethods(Method[] methods) {
        return Arrays.stream(methods).map(ComparedMethod::new)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Generates a list of unique methods for the given {@link Class} token.
     *
     * @param token the given {@link Class} token
     * @return a list of strings with methods sourcecode
     * @see ImplementorCodeGenerator#generateMethod(Method)
     */
    private static List<String> generateMethods(Class<?> token) {
        Set<ComparedMethod> methods = getComparedMethods(token.getMethods());
        while (token != null) {
            methods.addAll(getComparedMethods(token.getDeclaredMethods()));
            token = token.getSuperclass();
        }
        return methods.stream().map(ComparedMethod::getMethod)
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .map(ImplementorCodeGenerator::generateMethod)
                .collect(Collectors.toList());
    }

    /**
     * Generates a list of constructors for the given {@link Class} token.
     *
     * @param token the given {@link Class} token
     * @return a list of strings with constructors sourcecode
     * @throws ImplerException if no available constructors found
     * @see ImplementorCodeGenerator#generateConstructor(Constructor)
     */
    private static List<String> generateConstructors(Class<?> token) throws ImplerException {
        if (token.isInterface()) {
            return new ArrayList<>();
        }
        List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(c -> !Modifier.isPrivate(c.getModifiers())).collect(Collectors.toList());
        if (constructors.isEmpty()) {
            throw new ImplerException("At least one constructor required");
        }
        return constructors.stream().map(ImplementorCodeGenerator::generateConstructor).collect(Collectors.toList());
    }

    /**
     * Generates the sourcecode of class for the given {@link Class} token.
     *
     * @param token the given {@link Class} token
     * @return a string with the sourcecode for the given class
     * @throws ImplerException if an error during code generation occurred
     * @see ImplementorCodeGenerator#generateClassOpeningLine(Class)
     * @see ImplementorCodeGenerator#generatePackages(Class)
     * @see ImplementorCodeGenerator#generateConstructors(Class)
     * @see ImplementorCodeGenerator#generateMethods(Class)
     */
    static String generate(Class<?> token) throws ImplerException {
        return concatenate(NEW_LINE,
                generatePackages(token),
                generateClassOpeningLine(token),
                String.join(NEW_LINE, generateConstructors(token)),
                String.join(NEW_LINE, generateMethods(token)),
                END_OF_EXEC);
    }
}
