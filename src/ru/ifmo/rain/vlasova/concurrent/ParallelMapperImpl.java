package ru.ifmo.rain.vlasova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<R> mappingResult = mapperList.getMappingResult(closedThreads);
        mapperList.processRuntimeExceptions();
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
}
