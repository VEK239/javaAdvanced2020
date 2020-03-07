package ru.ifmo.rain.vlasova.arrayset;

import java.util.*;

public class ArrayNavigableSet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private Comparator<? super T> comparator;
    private final List<T> data;

    public ArrayNavigableSet() {
        this(Collections.emptyList(), null);
    }

    public ArrayNavigableSet(Comparator<? super T> cmp) {
        this(Collections.emptyList(), cmp);
    }

    public ArrayNavigableSet(Collection<? extends T> other) {
        this(other, null);
    }

    public ArrayNavigableSet(Collection<? extends T> other, Comparator<? super T> cmp) {
        comparator = cmp;
        TreeSet<T> tmp = new TreeSet<>(cmp);
        tmp.addAll(other);
        data = new ArrayList<>(tmp);
    }

    private ArrayNavigableSet(List<T> other, Comparator<? super T> cmp) {
        comparator = cmp;
        data = other;
    }

    private T findElement(T t, int negativeFix, int positiveFix) {
        int index = Collections.binarySearch(data, t, comparator);
        index = index >= 0 ? (index + positiveFix) : (-index - 1 - negativeFix);
        return (index >= 0 && index < size() ? data.get(index) : null);
    }

    @Override
    public T lower(T t) {
        return findElement(t, 1, -1);
    }

    @Override
    public T floor(T t) {
        return findElement(t, 1, 0);
    }

    @Override
    public T ceiling(T t) {
        return findElement(t, 0, 0);
    }

    @Override
    public T higher(T t) {
        return findElement(t, 0, 1);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("Set is mutable!");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("Set is mutable!");
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArrayNavigableSet<>(new ReversedList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private int indexBinarySearchGetter(T element) {
        int index = Collections.binarySearch(data, element, comparator);
        index = index >= 0 ? index : (-index - 1);
        return index;
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int fromIndex = indexBinarySearchGetter(fromElement) + (!fromInclusive && contains(fromElement) ? 1 : 0);
        int toIndex = indexBinarySearchGetter(toElement) + (toInclusive && contains(toElement) ? 1 : 0);
        return fromIndex < toIndex && toIndex <= size() ? new ArrayNavigableSet<>(data.subList(fromIndex, toIndex), comparator) :
                new ArrayNavigableSet<>(comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return subSet(isEmpty() ? null : first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return subSet(fromElement, inclusive, isEmpty() ? null : last(), true);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return subSet(isEmpty() ? null : first(), true, toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return subSet(fromElement, true, isEmpty() ? null : last(), true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if ((comparator != null ? comparator.compare(fromElement, toElement) :
                ((Comparable<T>) fromElement).compareTo(toElement)) > 0) {
            throw new IllegalArgumentException();
        }
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public int size() {
        return data.size();
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Object element) {
//        return data.contains(element);
        return Collections.binarySearch(data, (T) element, comparator) >= 0;
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
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

    public static class ReversedList<T> extends AbstractList<T> {
        private List<T> data;
        private boolean isReversed = true;

        ReversedList(ReversedList<T> list) {
            this.data = list.data;
            this.isReversed = !list.isReversed;
        }

        ReversedList(List<T> other) {
            data = other;
        }

        @Override
        public T get(int index) {
            return isReversed ? data.get(size() - index - 1) : data.get(index);
        }

        @Override
        public int size() {
            return data.size();
        }
    }
}
