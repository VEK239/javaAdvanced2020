package ru.ifmo.rain.vlasova.arrayset;

import java.util.*;

public class ArraySortedSet<T> extends AbstractSet<T> implements java.util.SortedSet<T> {
    private Comparator<? super T> comparator;
    private final List<T> data;

    public ArraySortedSet() {
        comparator = null;
        data = new ArrayList<>();
    }

    public ArraySortedSet(Comparator<? super T> cmp) {
        comparator = cmp;
        data = new ArrayList<>();
    }

    public ArraySortedSet(Collection<? extends T> other) {
        comparator = null;
        data = new ArrayList<>(new TreeSet<>(other));
    }

    public ArraySortedSet(Collection<? extends T> other, Comparator<? super T> cmp) {
        comparator = cmp;
        TreeSet<T> tmp = new TreeSet<>(cmp);
        tmp.addAll(other);
        data = new ArrayList<>(tmp);
    }

    private ArraySortedSet(List<T> other, Comparator<? super T> cmp) {
        comparator = cmp;
        data = other;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    public boolean contains(Object element) {
//        return data.contains(element);
        return Collections.binarySearch(data, (T) element, comparator) >= 0;
    }

    @Override
    public T first() {
        if (!data.isEmpty()) {
            return data.iterator().next();
        }
        throw new NoSuchElementException();
    }

    @Override
    public T last() {
        if (!data.isEmpty()) {
            return data.get(data.size() - 1);
        }
        throw new NoSuchElementException();
    }

    private java.util.SortedSet<T> subSet(T fromElement, T toElement, boolean isLastIncluded) {
        int fromIndex = Collections.binarySearch(data, fromElement, comparator);
        int toIndex = Collections.binarySearch(data, toElement, comparator);
        fromIndex = fromIndex >= 0 ? fromIndex : (-fromIndex - 1);
        toIndex = toIndex >= 0 ? toIndex : (-toIndex - 1);
        toIndex += (isLastIncluded ? 1 : 0);
        return fromIndex <= toIndex && toIndex <= size() ? new ArraySortedSet<>(data.subList(fromIndex, toIndex), comparator) :
                new ArraySortedSet<>(comparator);
    }

    @Override
    public java.util.SortedSet<T> headSet(T toElement) {
        return subSet(data.isEmpty() ? null : first(), toElement, false);
    }

    @Override
    public java.util.SortedSet<T> tailSet(T fromElement) {
        return subSet(fromElement, data.isEmpty() ? null : last(), true);
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if ((comparator != null ? comparator.compare(fromElement, toElement) :
                ((Comparable<T>) fromElement).compareTo(toElement)) > 0) {
            throw new IllegalArgumentException();
        }
        return subSet(fromElement, toElement, false);
    }
}