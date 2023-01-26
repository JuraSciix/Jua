package jua.utils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class List<E> implements Iterable<E> {

    public static class Node<E> {

        private final List<E> owner;

        public E value;

        private Node<E> prev, next;

        Node(List<E> owner, E value) {
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

    @SafeVarargs
    public static <E> List<E> of(E... elements) {
        List<E> list = new List<>();
        for (E element : elements) {
            list.add(element);
        }
        return list;
    }

    public static <E> List<E> of(Collection<? extends E> collection) {
        List<E> list = new List<>();
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

    public void addAll(List<? extends E> elements) {
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

    public List<E> intersection(List<? extends E> list) {
        List<E> intersection = new List<>();
        for (E element : this) {
            if (list.contains(element)) {
                intersection.add(element);
            }
        }
        return intersection;
    }

    public List<E> diff(List<? extends E> list) {
        List<E> diff = new List<>();
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

    public List<E> sum(List<? extends E> list) {
        List<E> sum = new List<>();
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
        List<?> list = (List<?>) o;
        // Проверяем все элементы множества A на принадлежность множеству B и наоборот.
        // Таким образом мы проверяем взаимно-однозначное соотношение.
        for (Object element : list) {
            if (!contains(element)) {
                return false;
            }
        }
        for (E element : this) {
            if (!list.contains(element)) {
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
