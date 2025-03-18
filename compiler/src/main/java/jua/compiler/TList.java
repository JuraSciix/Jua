package jua.compiler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

// Название класса не конечное, сложно придумать лаконичное имя.
public class TList<T> extends LinkedList<T> {

    public TList() {
        super();
    }

    public TList(Collection<? extends T> c) {
        super(c);
    }

    public static <T> TList<T> empty() {
        return new TList<>();
    }

    public static <T> TList<T> of(T value) {
        return new TList<>(Collections.singleton(value));
    }

    @Deprecated
    public interface PredicateOperator<T> {
        boolean test(boolean x, T value);
    }

    @Deprecated
    public static <T> boolean reduceBoolean(TList<T> list, boolean initial, PredicateOperator<T> predicate) {
        for (T item : list) {
            initial = predicate.test(initial, item);
        }
        return initial;
    }
}
