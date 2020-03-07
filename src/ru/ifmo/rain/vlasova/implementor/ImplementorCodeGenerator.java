package ru.ifmo.rain.vlasova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class ImplementorCodeGenerator {
    private static final String OPEN_BRACE = "(";
    private static final String CLOSE_BRACE = ")";
    private static final String END_OF_EXEC = "}";
    private static final String START_OF_EXEC = "{";
    private static final String SPACE = " ";
    private static final String END_OF_EXP = ";";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String EMPTY = "";
    private static final String TAB = "\t";


    private static class VariableCounter implements Supplier<String> {
        int id = 0;

        public String get() {
            return "var" + id++;
        }
    }

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

    private static String concatenate(String delimiter, String... args) {
        return String.join(delimiter, args);
    }

    private static String generatePackages(Class<?> token) {
        Package pack = token.getPackage();
        return (pack == null ? "" : concatenate(SPACE, "package", pack.getName(), END_OF_EXP, NEW_LINE));
    }

    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    private static String generateClassOpeningLine(Class<?> token) {
        return concatenate(SPACE,
                Modifier.toString(token.getModifiers() & ~Modifier.INTERFACE & ~Modifier.ABSTRACT),
                "class",
                getClassName(token),
                (token.isInterface() ? "implements" : "extends"),
                token.getCanonicalName(),
                START_OF_EXEC);
    }

    private static String generateExecutableOpeningLine(Executable executable, String name) {
        return concatenate(SPACE,
                Modifier.toString(executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT),
                name,
                OPEN_BRACE + getArguments(executable, true) + CLOSE_BRACE,
                getThrownExceptions(executable),
                START_OF_EXEC);
    }

    private static String generateMethod(Method method) {
        return concatenate(NEW_LINE,
                TAB + generateExecutableOpeningLine(method,
                        concatenate(SPACE, method.getReturnType().getCanonicalName(), method.getName())),
                concatenate(SPACE,TAB + TAB + "return", getDefaultValue(method.getReturnType())) + END_OF_EXP,
                TAB + END_OF_EXEC);
    }

    private static String generateConstructor(Constructor constructor) {
        return concatenate(NEW_LINE,
                TAB + generateExecutableOpeningLine(constructor,
                        getClassName(constructor.getDeclaringClass())),
                concatenate(EMPTY,TAB + TAB + "super", OPEN_BRACE, getArguments(constructor, false), CLOSE_BRACE) +
                        END_OF_EXP,
                TAB + END_OF_EXEC);
    }

    private static String getThrownExceptions(Executable executable) {
        Class[] exceptions = executable.getExceptionTypes();
        if (exceptions.length != 0) {
            return concatenate(SPACE, "throws",
                    Arrays.stream(exceptions).map(Class::getCanonicalName).collect(Collectors.joining(", ")));
        }
        return "";
    }

    private static String getArguments(Executable executable, boolean isDeclaration) {
        VariableCounter counter = new VariableCounter();
        return Arrays.stream(executable.getParameterTypes())
                .map(c -> ((isDeclaration ? c.getCanonicalName() + SPACE : EMPTY) + counter.get()))
                .collect(Collectors.joining(", "));
    }

    private static Set<ComparedMethod> getComparedMethods(Method[] methods) {
        return Arrays.stream(methods).map(ComparedMethod::new)
                .collect(Collectors.toCollection(HashSet::new));
    }

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

    static String generate(Class<?> token) throws ImplerException {
        return concatenate(NEW_LINE,
                generatePackages(token),
                generateClassOpeningLine(token),
                String.join(NEW_LINE, generateConstructors(token)),
                String.join(NEW_LINE, generateMethods(token)),
                END_OF_EXEC);
    }
}
