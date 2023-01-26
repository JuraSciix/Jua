package jua.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class CollectionUtils {

    public static IntArrayList mergeIntLists(IntArrayList lhs, IntArrayList rhs) {
        IntArrayList sum = new IntArrayList(lhs.size() + rhs.size());
        sum.addAll(lhs);
        sum.addAll(rhs);
        return sum;
    }

    private CollectionUtils() { Assert.error(); }
}
