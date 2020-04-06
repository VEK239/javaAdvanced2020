package ru.ifmo.rain.vlasova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.nCopies;

public class ParallelMapperImpl implements ParallelMapper {
    private boolean closedThreads = false;
    private List<Thread> threads;
    private final ArrayDeque<Runnable> tasks;

    public ParallelMapperImpl(int threadsCount) {
        tasks = new ArrayDeque<>();
        Runnable runner = () -> {
            try {
                while (!Thread.interrupted()) {
                    runTask();
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        };
        threads = Stream.generate(() -> new Thread(runner)).limit(threadsCount).collect(Collectors.toList());
        threads.forEach(Thread::start);
    }

    private void runTask() throws InterruptedException {
        Runnable task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
        }
        task.run();
    }

    private void processExceptions(MapperList<?, ?> futureList) {
        List<RuntimeException> runtimeExceptions = futureList.getRuntimeExceptions();
        if (!runtimeExceptions.isEmpty()) {
            RuntimeException exception = new RuntimeException("Runtime exception(s) occurred during execution");
            runtimeExceptions.forEach(exception::addSuppressed);
            throw exception;
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        MapperList<T, R> mapperList = new MapperList<>(args.size(), f);
        for (int i = 0; i < args.size(); i++) {
            T value = args.get(i);
            final int finalI = i;
            synchronized (tasks) {
                tasks.add(() -> mapperList.setNewValue(value, finalI));
                tasks.notify();
            }
        }
        List<R> mappingResult = mapperList.getMappingResult();
        processExceptions(mapperList);
        return mappingResult;
    }

    @Override
    public void close() {
        closedThreads = true;
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public class MapperList<T, R> {
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
            R appliedResult = null;
            try {
                appliedResult = f.apply(value);
            } catch (RuntimeException e) {
                synchronized (runtimeExceptions) {
                    runtimeExceptions.add(e);
                }
            } finally {
                synchronized (this) {
                    result.set(insertingIndex, appliedResult);
                    if (++index == result.size()) {
                        finish();
                    }
                }
            }
        }

        synchronized List<R> getMappingResult() throws InterruptedException {
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

        List<RuntimeException> getRuntimeExceptions() {
            return runtimeExceptions;
        }
    }
}
