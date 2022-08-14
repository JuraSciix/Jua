package jua.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class Collections {

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
}
