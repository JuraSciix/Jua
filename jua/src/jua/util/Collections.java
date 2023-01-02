package jua.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class Collections {

    public static <T> void intersection(
            Iterable<? extends Collection<? extends T>> collections,
            Collection<? super T> output
    ) {
        Iterator<? extends Collection<? extends T>> iterator = collections.iterator();
        if (iterator.hasNext()) {
            HashSet<T> buf = new HashSet<>(iterator.next());
            while (iterator.hasNext()) {
                buf.retainAll(iterator.next());
            }
            output.addAll(buf);
        }
    }

    public static <T> void join(
            Iterable<Collection<? extends T>> collections,
            Collection<? super T> output
    ) {
        for (Collection<? extends T> collection : collections) {
            output.addAll(collection);
        }
    }

    public static <T> void doubleForEach(Collection<? extends T> c1, Collection<? extends T> c2, BiConsumer<? super T, ? super T> action) {
        if (c1.size() != c2.size()) {
            throw new IllegalArgumentException("Different sizes");
        }

        Iterator<? extends T> i1 = c1.iterator();
        Iterator<? extends T> i2 = c2.iterator();

        while (i1.hasNext()) {
            action.accept(i1.next(), i2.next());
        }
    }

    public static IntArrayList mergeIntLists(IntArrayList lhs, IntArrayList rhs) {
        IntArrayList sum = new IntArrayList(lhs.size() + rhs.size());
        sum.addAll(lhs);
        sum.addAll(rhs);
        return sum;
    }
}
