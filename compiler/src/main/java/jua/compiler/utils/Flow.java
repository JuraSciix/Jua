package jua.compiler.utils;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

// Название класса не конечное, сложно придумать лаконичное имя.
public class Flow<T> {

    public static class Builder<T> {
        private Flow<T> head, tail;

        Builder(Flow<T> head, Flow<T> tail) {
            this.head = head;
            this.tail = tail;
        }

        public Builder<T> append(T value) {
            Flow<T> newFlow = new Flow<>(value);
            if (tail != null) {
                tail.next = newFlow;
            } else {
                head = newFlow;
            }
            tail = newFlow;
            return this;
        }

        public Flow<T> toFlow() {
            return head;
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>(null, null);
    }

    public static <T> Builder<T> builder(Flow<T> flow) {
        return new Builder<>(flow, getTail(flow));
    }

    private static <T> Flow<T> getTail(Flow<T> flow) {
        Flow<T> tail = flow;
        if (tail != null) {
            while (tail.next != null) {
                tail = tail.next;
            }
        }
        return tail;
    }

    public static <T> Flow<T> empty() {
        return null;
    }

    public static <T> Flow<T> of(T value) {
        return new Flow<>(value);
    }

    public static <T> void forEach(Flow<T> flow, Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (Flow<T> f = flow; f != null; f = f.next) {
            if (f == f.next) {
                throw new AssertionError("Self-reference");
            }
            action.accept(f.value);
        }
    }

    public static <T> void translate(Flow<T> flow, UnaryOperator<T> translator) {
        Objects.requireNonNull(translator);
        for (Flow<T> f = flow; f != null; f = f.next) {
            f.value = translator.apply(f.value);
        }
    }

    public static <T, A> A reduce(Flow<T> flow, A initial, BiFunction<T, A, A> accumulator) {
        Objects.requireNonNull(accumulator);
        A a = initial;
        for (Flow<T> f = flow; f != null; f = f.next) {
            a = accumulator.apply(f.value, a);
        }
        return a;
    }

    public static <T> boolean allMatch(Flow<T> flow, Predicate<T> predicate) {
        return !test(flow, predicate, false);
    }

    public static <T> boolean noneMatch(Flow<T> flow, Predicate<T> predicate) {
        return !test(flow, predicate, true);
    }

    public static <T> boolean anyMatch(Flow<T> flow, Predicate<T> predicate) {
        return test(flow, predicate, true);
    }

    private static <T> boolean test(Flow<T> flow, Predicate<T> predicate, boolean interruptIf) {
        Objects.requireNonNull(predicate);
        for (Flow<T> f = flow; f != null; f = f.next) {
            if (predicate.test(f.value) == interruptIf) {
                return true;
            }
        }
        return false;
    }

    public static int count(Flow<?> flow) {
        // Заметка: так как это связный список, то count может быть больше,
        // чем Integer.MAX_VALUE, но полагаться на long слишком неэффективно.
        int count = 0;
        for (Flow<?> f = flow; f != null; f = f.next) {
            count++;
        }
        return count;
    }

    public T value;

    public Flow<T> next;

    Flow(T value) {
        this.value = value;
    }
}
