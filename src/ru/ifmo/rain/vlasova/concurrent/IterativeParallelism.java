package ru.ifmo.rain.vlasova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class IterativeParallelism implements ListIP {
    private final ParallelMapper mapper;

    public IterativeParallelism() {
        this.mapper = null;
    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Generates participation of given values into smaller streams for parallel computation.
     *
     * @param threadsCount the given count of threads to participate the computation
     * @param values       the {@link List} of given values to participate
     * @return participated tasks
     */
    private <T> List<Stream<? extends T>> participate(List<? extends T> values, int threadsCount) {
        List<Stream<? extends T>> tasks = new ArrayList<>();
        int valuesPerThread = values.size() / threadsCount;
        int remainingValues = values.size() % threadsCount;
        for (int i = 0; i < threadsCount; ++i) {
            int from = i * valuesPerThread + min(i, remainingValues);
            int to = from + valuesPerThread + (i < remainingValues ? 1 : 0);
            tasks.add(values.subList(from, to).stream());
        }
        return tasks;
    }

    /**
     * Realises parallel computations of the given list of objects.
     *
     * @param threadsCount the given count of threads to participate the computation
     * @param values       the {@link List} of given values to count
     * @param task         an associative function to apply to every value
     * @param collector    a function to collect computed values into resulting value
     * @return the result of applying a function to the given values
     * @throws InterruptedException if an error during parallel equations occurred
     */
    private <T, R> R doTask(int threadsCount, List<? extends T> values,
                            Function<Stream<? extends T>, ? extends R> task,
                            Function<Stream<? extends R>, ? extends R> collector) throws InterruptedException {
        threadsCount = max(1, min(values.size(), threadsCount));
        List<Stream<? extends T>> tasks = participate(values, threadsCount);

        List<R> result;
        if (mapper == null) {

            result = new ArrayList<>(Collections.nCopies(threadsCount, null));
            List<Thread> workers = new ArrayList<>();

            for (int section = 0; section < threadsCount; ++section) {
                int finalSection = section;
                Thread thread = new Thread(() -> result.set(finalSection, task.apply(tasks.get(finalSection))));
                workers.add(thread);
                thread.start();
            }
            joinThreads(workers);
        } else {
            result = mapper.map(task, tasks);
        }
        return collector.apply(result.stream());
    }

    private void joinThreads(List<Thread> workers) throws InterruptedException {
        InterruptedException interruptedException = null;
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                if (interruptedException == null) {
                    interruptedException = new InterruptedException();
                }
                interruptedException.addSuppressed(e);
            }
        }
        if (interruptedException != null) {
            throw interruptedException;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, ? extends T> maxStream = stream -> stream.max(comparator).orElse(null);
        return doTask(threads, values, maxStream, maxStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, ? extends T> minStream = stream -> stream.min(comparator).orElse(null);
        return doTask(threads, values, minStream, minStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return doTask(threads, values, stream -> stream.allMatch(predicate), stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return doTask(threads, values, stream -> stream.anyMatch(predicate), stream -> stream.anyMatch(Boolean::booleanValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return doTask(threads, values, stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return doTask(threads, values, stream -> stream.filter(predicate).collect(Collectors.toList()),
                list -> list.flatMap(List::stream).collect(Collectors.toList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return doTask(threads, values, stream -> stream.map(f).collect(Collectors.toList()),
                list -> list.flatMap(List::stream).collect(Collectors.toList()));
    }
}
