package jua.utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JuaList<E> implements Iterable<E> {

    public static <T> Collector<T, JuaList<T>, JuaList<T>> collector() {
        return Collector.of(JuaList::new, JuaList::add, (l1, l2) -> { l1.addAll(l2); return l1; });
    }

    public static class Node<E> {

        private final JuaList<E> owner;

        public E value;

        private Node<E> prev, next;

        Node(JuaList<E> owner, E value) {
            this.owner = owner;
            this.value = value;

            owner.count++;
        }

        public Node<E> prev() { return prev; }

        public Node<E> next() { return next; }

        public Node<E> linkAfter(E element) {
            Node<E> newNode = new Node<>(owner, element);
            newNode.prev = this;
            if (next != null) {
                next.prev = newNode;
                newNode.next = next;
            }
            next = newNode;
            return newNode;
        }

        public Node<E> linkBefore(E element) {
            Node<E> newNode = new Node<>(owner, element);
            if (prev != null) {
                prev.next = newNode;
                newNode.prev = prev;
            }
            prev = newNode;
            newNode.next = this;
            return newNode;
        }

        public void unlink() {
            if (prev != null) {
                prev.next = next;
            }
            if (next != null) {
                next.prev = prev;
            }
            owner.count--;
        }
    }

    public static <E> JuaList<E> empty() {
        return new JuaList<>();
    }

    public static <E> JuaList<E> of(E e) {
        JuaList<E> list = new JuaList<>();
        list.add(e);
        return list;
    }

    public static <E> JuaList<E> of(E e1, E e2) {
        JuaList<E> list = new JuaList<>();
        list.add(e1);
        list.add(e2);
        return list;
    }

    public static <E> JuaList<E> of(E e1, E e2, E e3) {
        JuaList<E> list = new JuaList<>();
        list.add(e1);
        list.add(e2);
        list.add(e3);
        return list;
    }

    @SafeVarargs
    public static <E> JuaList<E> of(E... elements) {
        JuaList<E> list = new JuaList<>();
        for (E element : elements) {
            list.add(element);
        }
        return list;
    }

    public static <E> JuaList<E> of(Collection<? extends E> collection) {
        JuaList<E> list = new JuaList<>();
        for (E element : collection) {
            list.add(element);
        }
        return list;
    }

    private class Itr implements Iterator<E> {

        private Node<E> node = head;

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E e = node.value;
            node = node.next;
            return e;
        }

        @Override
        public void remove() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            node.unlink();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /** Первый узел */
    Node<E> head;

    /** Последний узел */
    Node<E> tail;

    int count = 0;

    public void add(E element) {
        if (head == null) {
            head = tail = new Node<>(this, element);
        } else {
            tail = tail.linkAfter(element);
        }
    }

    public void addAll(JuaList<? extends E> elements) {
        for (E element : elements) {
            add(element);
        }
    }

    public Node<E> head() { return head; }

    public Node<E> tail() { return tail; }

    public E first() {
        ensureHead();
        return head.value;
    }

    public E last() {
        ensureHead();
        return tail.value;
    }

    /** Вставляет элемент в начало списка. */
    public void push(E element) {
        if (head == null) {
            head = tail = new Node<>(this, element);
        } else {
            head = head.linkBefore(element);
        }
    }

    /** Удаляет и возвращает первый элемент списка. */
    public E pop() {
        ensureHead();
        E e = head.value;
        head = head.next;
        count--;
        if (head == null) tail = null;
        return e;
    }

    /** Вставляет элемент в конец списка */
    public void offer(E element) {
        add(element);
    }

    /** Удаляет и возвращает последний элемент списка. */
    public E poll() {
        ensureHead();
        E e = tail.value;
        tail = tail.prev;
        count--;
        if (tail == null) head = null;
        return e;
    }

    private void ensureHead() {
        if (head == null) {
            throw new NoSuchElementException("List is empty");
        }
    }

    public int count() { return count; }

    public boolean isEmpty() { return count() == 0; }

    public boolean nonEmpty() { return !isEmpty(); }

    public void clear() {
        head = tail = null;
        count = 0;
    }

    public boolean contains(Object o) {
        for (E element : this) {
            if (Objects.equals(element, o)) {
                return true;
            }
        }
        return false;
    }

    public JuaList<E> intersection(JuaList<? extends E> list) {
        JuaList<E> intersection = new JuaList<>();
        for (E element : this) {
            if (list.contains(element)) {
                intersection.add(element);
            }
        }
        return intersection;
    }

    public JuaList<E> diff(JuaList<? extends E> list) {
        JuaList<E> diff = new JuaList<>();
        for (E element : this) {
            if (!list.contains(element)) {
                diff.add(element);
            }
        }
        for (E element : list) {
            if (!contains(element)) {
                diff.add(element);
            }
        }
        return diff;
    }

    public JuaList<E> sum(JuaList<? extends E> list) {
        JuaList<E> sum = new JuaList<>();
        sum.addAll(this);
        sum.addAll(list);
        return sum;
    }

    public int removeIf(Predicate<? super E> filter) {
        Iterator<E> iterator = iterator();
        int modCount = 0;
        while (iterator.hasNext()) {
            if (filter.test(iterator.next())) {
                iterator.remove();
                modCount++;
            }
        }
        return modCount;
    }

    public <U> JuaList<U> map(Function<? super E, ? extends U> mapper) {
        JuaList<U> mappedList = new JuaList<>();

        for (E element : this) {
            mappedList.add(mapper.apply(element));
        }

        return mappedList;
    }

    public <U> JuaList<U> flatMap(Function<? super E, ? extends JuaList<U>> mapper) {
        JuaList<U> mappedList = new JuaList<>();

        for (E element : this) {
            mappedList.addAll(mapper.apply(element));
        }

        return mappedList;
    }

    public E[] toArray(IntFunction<? extends E[]> generator) {
        E[] array = generator.apply(count);
        Node<E> node = head;
        for (int i = 0; i < count; i++) {
            array[i] = node.value;
            node = node.next;
        }
        return array;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        for (E element : this) {
            hash = hash * 17 + Objects.hashCode(element);
        }

        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        JuaList<?> l = (JuaList<?>) o;
        // Определяем равенство на основе биекции множеств A и B.
        for (Object element : l) {
            if (!contains(element)) {
                return false;
            }
        }
        for (E element : this) {
            if (!l.contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringJoiner buffer = new StringJoiner(", ", "[", "]");

        for (E element : this) {
            buffer.add(String.valueOf(element));
        }
        
        return buffer.toString();
    }
}
