package jua.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;

public class ObjectSizeCalculating {

    /** Размер заголовка объекта в байтах. Равно 16 на 32-битных платформах и 8 на 64-битных. */
    private static final long HEADER_SIZE;

    /** Размер ссылки в байтах. Равно 4 на 32-битных платформах и 8 на 64-битных. */
    private static final long REF_SIZE;

    private static final HashMap<Class<?>, TSizeCalc<?>> tSizeCalcMap = new HashMap<>();

    static {
        if (System.getProperty("java.vm.name").contains("64")) {
            // java.vm.name is something like "Java HotSpot(TM) 64-Bit Server VM"
            HEADER_SIZE = 16;
            REF_SIZE = 8;
        } else {
            HEADER_SIZE = 8;
            REF_SIZE = 4;
        }
        
        tSizeCalcMap.put(Integer.class,     new JavaLangIntegerSizeCalc());
        tSizeCalcMap.put(Long.class,        new JavaLangLongSizeCalc());
        tSizeCalcMap.put(Float.class,       new JavaLangFloatSizeCalc());
        tSizeCalcMap.put(Double.class,      new JavaLangDoubleSizeCalc());
        tSizeCalcMap.put(Byte.class,        new JavaLangByteSizeCalc());
        tSizeCalcMap.put(Short.class,       new JavaLangShortSizeCalc());
        tSizeCalcMap.put(Character.class,   new JavaLangCharacterSizeCalc());
        tSizeCalcMap.put(Boolean.class,     new JavaLangBooleanSizeCalc());
        tSizeCalcMap.put(String.class,      new JavaLangStringSizeCalc());
        tSizeCalcMap.put(int[].class,       new IntArraySizeCalc());
        tSizeCalcMap.put(long[].class,      new LongArraySizeCalc());
        tSizeCalcMap.put(float[].class,     new FloatArraySizeCalc());
        tSizeCalcMap.put(double[].class,    new DoubleArraySizeCalc());
        tSizeCalcMap.put(byte[].class,      new ByteArraySizeCalc());
        tSizeCalcMap.put(short[].class,     new ShortArraySizeCalc());
        tSizeCalcMap.put(char[].class,      new CharArraySizeCalc());
        tSizeCalcMap.put(boolean[].class,   new BooleanArraySizeCalc());
        tSizeCalcMap.put(Object[].class,    new ObjectArraySizeCalc());
        tSizeCalcMap.put(Object.class,      new ObjectSizeCalc());
    }
    
    public static long calcSizeOf(Object instance) {
        return calcSizeOf(instance, new HashSet<>());
    }

    static class ObjectWrapper {
        
        final Object source;

        ObjectWrapper(Object source) {
            this.source = source;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(source);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            return source == ((ObjectWrapper) o).source;
        }
    }

    @SuppressWarnings("unchecked")
    static long calcSizeOf(Object instance, HashSet<ObjectWrapper> dejaVu) {
        if (instance == null) return 0L;
        if (!dejaVu.add(new ObjectWrapper(instance))) return REF_SIZE;
        tSizeCalcMap.computeIfAbsent(instance.getClass(), ObjectSizeCalculating::generateSizeCalcFor);
        TSizeCalc<Object> sizeCalc = (TSizeCalc<Object>) tSizeCalcMap.get(instance.getClass());
        return align(sizeCalc.sizeOf(instance, dejaVu));
    }

    static <T> TSizeCalc<T> generateSizeCalcFor(Class<T> klass) {
        return (t, dejaVu) -> {
            Class<?> superclass = klass;
            long size = HEADER_SIZE;

            do {
                for (Field field : superclass.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    Class<?> type = field.getType();
                    if (type == int.class || type == float.class) {
                        size += 4;
                        continue;
                    }
                    if (type == long.class || type == double.class) {
                        size += 8;
                        continue;
                    }
                    if (type == byte.class || type == boolean.class) {
                        size += 1;
                        continue;
                    }
                    if (type == short.class || type == char.class) {
                        size += 2;
                        continue;
                    }
                    size += REF_SIZE;
                    Object value;
                    try {
                        field.setAccessible(true);
                        value = field.get(t);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        throw new AssertionError(e); // Не должно случиться
                    }
                    size += calcSizeOf(value, dejaVu);
                }
                superclass = superclass.getSuperclass();
            } while (superclass != Object.class && superclass != null);

            return size;
        };
    }

    interface TSizeCalc<T> { long sizeOf(T t, HashSet<ObjectWrapper> dejaVu); }

    static class JavaLangIntegerSizeCalc implements TSizeCalc<Integer> {
        @Override
        public long sizeOf(Integer integer, HashSet<ObjectWrapper> dejaVu) {
            return HEADER_SIZE + 4;
        }
    }

    static class JavaLangLongSizeCalc implements TSizeCalc<Long> {
        @Override
        public long sizeOf(Long aLong, HashSet<ObjectWrapper> dejaVu) {
            return HEADER_SIZE + 8;
        }
    }

    static class JavaLangFloatSizeCalc implements TSizeCalc<Float> {
        @Override
        public long sizeOf(Float aFloat, HashSet<ObjectWrapper> dejaVu) {
            return HEADER_SIZE + 4;
        }
    }

    static class JavaLangDoubleSizeCalc implements TSizeCalc<Double> {
        @Override
        public long sizeOf(Double aDouble, HashSet<ObjectWrapper> dejaVu) {
            return HEADER_SIZE + 8;
        }
    }

    static class JavaLangByteSizeCalc implements TSizeCalc<Byte> {
        @Override
        public long sizeOf(Byte aByte, HashSet<ObjectWrapper> dejaVu) {
            return HEADER_SIZE + 1;
        }
    }

    static class JavaLangShortSizeCalc implements TSizeCalc<Short> {
        @Override
        public long sizeOf(Short aShort, HashSet<ObjectWrapper> dejaVu) {
            return HEADER_SIZE + 2;
        }
    }

    static class JavaLangCharacterSizeCalc implements TSizeCalc<Character> {
        @Override
        public long sizeOf(Character character, HashSet<ObjectWrapper> dejaVu) {
            return HEADER_SIZE + 2;
        }
    }

    static class JavaLangBooleanSizeCalc implements TSizeCalc<Boolean> {
        @Override
        public long sizeOf(Boolean aBoolean, HashSet<ObjectWrapper> dejaVu) {
            return HEADER_SIZE + 1;
        }
    }

    static class JavaLangStringSizeCalc implements TSizeCalc<String> {
        @Override
        public long sizeOf(String string, HashSet<ObjectWrapper> dejaVu) {
            return (HEADER_SIZE + REF_SIZE + 4L) +        // java.lang.String.(value:C[;hash:I)
                    (HEADER_SIZE + 2L * string.length()); // java.lang.String.value:[C
        }
    }

    static class IntArraySizeCalc implements TSizeCalc<int[]> {
        @Override
        public long sizeOf(int[] ints, HashSet<ObjectWrapper> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 4L * ints.length;
        }
    }

    static class LongArraySizeCalc implements TSizeCalc<long[]> {
        @Override
        public long sizeOf(long[] longs, HashSet<ObjectWrapper> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 8L * longs.length;
        }
    }

    static class FloatArraySizeCalc implements TSizeCalc<float[]> {
        @Override
        public long sizeOf(float[] floats, HashSet<ObjectWrapper> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 4L * floats.length;
        }
    }

    static class DoubleArraySizeCalc implements TSizeCalc<double[]> {
        @Override
        public long sizeOf(double[] longs, HashSet<ObjectWrapper> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 8L * longs.length;
        }
    }

    static class ByteArraySizeCalc implements TSizeCalc<byte[]> {
        @Override
        public long sizeOf(byte[] bytes, HashSet<ObjectWrapper> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 1L * bytes.length;
        }
    }

    static class ShortArraySizeCalc implements TSizeCalc<short[]> {
        @Override
        public long sizeOf(short[] shorts, HashSet<ObjectWrapper> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 2L * shorts.length;
        }
    }

    static class CharArraySizeCalc implements TSizeCalc<char[]> {
        @Override
        public long sizeOf(char[] chars, HashSet<ObjectWrapper> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 2L * chars.length;
        }
    }

    static class BooleanArraySizeCalc implements TSizeCalc<boolean[]> {
        @Override
        public long sizeOf(boolean[] booleans, HashSet<ObjectWrapper> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 1L * booleans.length;
        }
    }

    static class ObjectArraySizeCalc implements TSizeCalc<Object[]> {
        @Override
        public long sizeOf(Object[] objects, HashSet<ObjectWrapper> dejaVu) {
            // Размер массива хранится в заголовке.
            long size = HEADER_SIZE + REF_SIZE * objects.length;
            for (Object object : objects) {
                size += calcSizeOf(object, dejaVu);
            }
            return size;
        }
    }

    static class ObjectSizeCalc implements TSizeCalc<Object> {
        @Override
        public long sizeOf(Object o, HashSet<ObjectWrapper> dejaVu) {
            return HEADER_SIZE;
        }
    }

    private static long align(long a) { return (a + 8 - 1) & -8; }

    private ObjectSizeCalculating() { Assert.error(); }
}
