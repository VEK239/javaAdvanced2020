package ru.ifmo.rain.vlasova.concurrent;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.nCopies;

class MapperList<T, R> {
    private List<R> result;
    private final Function<? super T, ? extends R> f;
    private int index = 0;
    private boolean finishedMapping = false;
    private final List<RuntimeException> runtimeExceptions;

    MapperList(int size, Function<? super T, ? extends R> f) {
        runtimeExceptions = new ArrayList<>();
        result = new ArrayList<>(nCopies(size, null));
        this.f = f;
    }

    void setNewValue(T value, int insertingIndex) {
        try {
            R appliedResult = f.apply(value);
            synchronized (this) {
                result.set(insertingIndex, appliedResult);
                if (++index == result.size()) {
                    finish();
                }
            }
        } catch (RuntimeException e) {
            runtimeExceptions.add(e);
        }
    }

    synchronized List<R> getMappingResult(boolean closedThreads) throws InterruptedException {
        while (!finishedMapping && !closedThreads) {
            wait();
        }
        return result;
    }

    private synchronized void finish() {
        if (!finishedMapping) {
            finishedMapping = true;
            notify();
        }
    }

    void processRuntimeExceptions() throws RuntimeException {
        if (!runtimeExceptions.isEmpty()) {
            RuntimeException exception = new RuntimeException("Runtime exceptions occurred.");
            runtimeExceptions.forEach(exception::addSuppressed);
            throw exception;
        }
    }
}