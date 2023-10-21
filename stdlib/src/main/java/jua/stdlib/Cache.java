package jua.stdlib;


import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.function.Function;

public class Cache<K, V> {

    private final HashMap<K, SoftReference<V>> cache;

    public Cache() {
        cache = new HashMap<>();
    }

    public Cache(int initialCapacity) {
        cache = new HashMap<>(initialCapacity);
    }

    public Cache(int initialCapacity, float loadFactor) {
        cache = new HashMap<>(initialCapacity, loadFactor);
    }

    public V access(K key, Function<? super K, ? extends V> generator) {
        SoftReference<V> ref = cache.get(key);
        if (isRefInvalid(ref)) {
            synchronized (monitor(key)) {
                ref = cache.get(key);
                if (isRefInvalid(ref)) {
                    V val = generator.apply(key);
                    cache.put(key, new SoftReference<>(val));
                    return val;
                }
            }
        }
        return ref.get();
    }

    private static boolean isRefInvalid(SoftReference<?> ref) {
        return ref == null || ref.isEnqueued();
    }

    private static final Object NULL_MONITOR = new Object();

    private static Object monitor(Object key) {
        if (key == null) return NULL_MONITOR;
        return key;
    }
}
