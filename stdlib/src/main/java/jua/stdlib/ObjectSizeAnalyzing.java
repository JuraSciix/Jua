package jua.stdlib;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Утилитарный класс, занимающийся глубоким анализом занимаемой объектами памяти.
 */
public class ObjectSizeAnalyzing {

    /** Байтовый размер заголовка объекта. Равен 16 на 32-битных платформах и 8 на 64-битных. */
    private static final long HEADER_SIZE;

    /** Байтовый размер указателя. Равен 4 на 32-битных платформах и 8 на 64-битных. */
    private static final long POINTER_SIZE;

    /** Анализаторы некоторых внутренних классов. */
    private static final HashMap<Class<?>, ObjectAnalyzer<?>> systemAnalyzers = new HashMap<>();

    /** Анализаторы внешних классов. */
    private static final Cache<Class<?>, ReflectAnalyzer> cachedAnalyzers = new Cache<>();

    static {
        if (PlatformUtils.is64Bit()) {
            HEADER_SIZE = 16;
            POINTER_SIZE = 8;
        } else {
            HEADER_SIZE = 8;
            POINTER_SIZE = 4;
        }

        systemAnalyzers.put(int[].class, new IntArrayAnalyzer());
        systemAnalyzers.put(long[].class, new LongArrayAnalyzer());
        systemAnalyzers.put(float[].class, new FloatArrayAnalyzer());
        systemAnalyzers.put(double[].class, new DoubleArrayAnalyzer());
        systemAnalyzers.put(byte[].class, new ByteArrayAnalyzer());
        systemAnalyzers.put(short[].class, new ShortArrayAnalyzer());
        systemAnalyzers.put(char[].class, new CharArrayAnalyzer());
        systemAnalyzers.put(boolean[].class, new BooleanArrayAnalyzer());
        systemAnalyzers.put(Object[].class, new ReferenceArrayAnalyzer());
        systemAnalyzers.put(Object.class, new JavaLangObjectAnalyzer());
        systemAnalyzers.put(Integer.class, new JavaLangIntegerAnalyzer());
        systemAnalyzers.put(Long.class, new JavaLangLongAnalyzer());
        systemAnalyzers.put(Float.class, new JavaLangFloatAnalyzer());
        systemAnalyzers.put(Double.class, new JavaLangDoubleAnalyzer());
        systemAnalyzers.put(Byte.class, new JavaLangByteAnalyzer());
        systemAnalyzers.put(Short.class, new JavaLangShortAnalyzer());
        systemAnalyzers.put(Character.class, new JavaLangCharacterAnalyzer());
        systemAnalyzers.put(Boolean.class, new JavaLangBooleanAnalyzer());
        // Структура java.lang.String менялась в различных версиях JDK и может меняться впредь.
//        tAnalyzerMap.put(String.class,      new JavaLangStringAnalyzer());
    }

    private static abstract class ObjectAnalyzer<T> {
        abstract long sizeOf(T t, HashSet<ObjectRef> dejaVu);
    }

    private static class JavaLangObjectAnalyzer extends ObjectAnalyzer<Object> {
        @Override
        long sizeOf(Object o, HashSet<ObjectRef> dejaVu) {
            return HEADER_SIZE;
        }
    }

    private static class JavaLangIntegerAnalyzer extends ObjectAnalyzer<Integer> {
        @Override
        long sizeOf(Integer i, HashSet<ObjectRef> dejaVu) {
            return HEADER_SIZE + 4;
        }
    }

    private static class JavaLangLongAnalyzer extends ObjectAnalyzer<Long> {
        @Override
        long sizeOf(Long l, HashSet<ObjectRef> dejaVu) {
            return HEADER_SIZE + 8;
        }
    }

    private static class JavaLangFloatAnalyzer extends ObjectAnalyzer<Float> {
        @Override
        long sizeOf(Float f, HashSet<ObjectRef> dejaVu) {
            return HEADER_SIZE + 4;
        }
    }

    private static class JavaLangDoubleAnalyzer extends ObjectAnalyzer<Double> {
        @Override
        long sizeOf(Double d, HashSet<ObjectRef> dejaVu) {
            return HEADER_SIZE + 8;
        }
    }

    private static class JavaLangByteAnalyzer extends ObjectAnalyzer<Byte> {
        @Override
        long sizeOf(Byte b, HashSet<ObjectRef> dejaVu) {
            return HEADER_SIZE + 1;
        }
    }

    private static class JavaLangShortAnalyzer extends ObjectAnalyzer<Short> {
        @Override
        long sizeOf(Short s, HashSet<ObjectRef> dejaVu) {
            return HEADER_SIZE + 2;
        }
    }

    private static class JavaLangCharacterAnalyzer extends ObjectAnalyzer<Character> {
        @Override
        long sizeOf(Character c, HashSet<ObjectRef> dejaVu) {
            return HEADER_SIZE + 2;
        }
    }

    private static class JavaLangBooleanAnalyzer extends ObjectAnalyzer<Boolean> {
        @Override
        long sizeOf(Boolean b, HashSet<ObjectRef> dejaVu) {
            return HEADER_SIZE + 1;
        }
    }

    private static class IntArrayAnalyzer extends ObjectAnalyzer<int[]> {
        @Override
        long sizeOf(int[] ia, HashSet<ObjectRef> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 4L * ia.length;
        }
    }

    private static class LongArrayAnalyzer extends ObjectAnalyzer<long[]> {
        @Override
        long sizeOf(long[] la, HashSet<ObjectRef> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 8L * la.length;
        }
    }

    private static class FloatArrayAnalyzer extends ObjectAnalyzer<float[]> {
        @Override
        long sizeOf(float[] fa, HashSet<ObjectRef> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 4L * fa.length;
        }
    }

    private static class DoubleArrayAnalyzer extends ObjectAnalyzer<double[]> {
        @Override
        long sizeOf(double[] da, HashSet<ObjectRef> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 8L * da.length;
        }
    }

    private static class ByteArrayAnalyzer extends ObjectAnalyzer<byte[]> {
        @Override
        long sizeOf(byte[] ba, HashSet<ObjectRef> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 1L * ba.length;
        }
    }

    private static class ShortArrayAnalyzer extends ObjectAnalyzer<short[]> {
        @Override
        long sizeOf(short[] sa, HashSet<ObjectRef> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 2L * sa.length;
        }
    }

    private static class CharArrayAnalyzer extends ObjectAnalyzer<char[]> {
        @Override
        long sizeOf(char[] ca, HashSet<ObjectRef> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 2L * ca.length;
        }
    }

    private static class BooleanArrayAnalyzer extends ObjectAnalyzer<boolean[]> {
        @Override
        long sizeOf(boolean[] ba, HashSet<ObjectRef> dejaVu) {
            // Размер массива хранится в заголовке.
            return HEADER_SIZE + 1L * ba.length;
        }
    }

    private static class ReferenceArrayAnalyzer extends ObjectAnalyzer<Object[]> {
        @Override
        long sizeOf(Object[] oa, HashSet<ObjectRef> dejaVu) {
            // Размер массива хранится в заголовке.
            long size = HEADER_SIZE + POINTER_SIZE * oa.length;
            for (Object object : oa) {
                size += analyzeSize(object, dejaVu);
            }
            return size;
        }
    }

    private static class ObjectRef {
        private final Object source;

        ObjectRef(Object source) {
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
            return source == ((ObjectRef) o).source;
        }
    }

    private static class ReflectAnalyzer {

        private final Class<?> klass;
        private final long primitiveSize;
        private final Field[] pointerFields;

        ReflectAnalyzer(Class<?> klass) {
            long primitiveSize = HEADER_SIZE;
            ArrayList<Field> pointerFields = new ArrayList<>();

            Class<?> curKlass = klass;
            while (curKlass != null && curKlass != Object.class) {
                for (Field field : curKlass.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers)) {
                        continue;
                    }
                    Class<?> type = field.getType();
                    if (type == byte.class || type == boolean.class) {
                        primitiveSize += 1;
                        continue;
                    }
                    if (type == short.class || type == char.class) {
                        primitiveSize += 2;
                        continue;
                    }
                    if (type == int.class || type == float.class) {
                        primitiveSize += 4;
                        continue;
                    }
                    if (type == long.class || type == double.class) {
                        primitiveSize += 8;
                        continue;
                    }
                    pointerFields.add(field);
                }
                curKlass = curKlass.getSuperclass();
            }

            this.klass = klass;
            this.primitiveSize = primitiveSize + POINTER_SIZE * pointerFields.size();
            this.pointerFields = pointerFields.toArray(new Field[0]);
        }

        long sizeOf(Object instance, HashSet<ObjectRef> dejaVu) throws Exception {
            if (instance.getClass() != klass) {
                throw new IllegalArgumentException();
            }
            // primitiveSize = HEADER_SIZE + PRIMITIVE_FIELDS + POINTER_SIZE * pointerFields.length
            long size = primitiveSize;

            for (Field field : pointerFields) {
                field.setAccessible(true);
                size += analyzeSize(field.get(instance), dejaVu);
            }

            return size;
        }
    }

    public static long analyzeSize(Object instance) {
        if (instance == null) return 0L; // quick null-check before java.util.HashSet allocation
        return analyzeSize(instance, new HashSet<>());
    }

    private static long analyzeSize(Object instance, HashSet<ObjectRef> dejaVu) {
        if (instance == null) return 0L;
        if (!dejaVu.add(new ObjectRef(instance))) return POINTER_SIZE; // instance already analyzed
        Class<?> klass = instance.getClass();
        if (klass.isArray() && !klass.getComponentType().isPrimitive())
            klass = Object[].class; // Хитрая оптимизация: для любого T верно, что T[] -> Object[]
        ObjectAnalyzer<?> systemAnalyzer = systemAnalyzers.get(klass);
        if (systemAnalyzer != null) {
            @SuppressWarnings("unchecked")
            ObjectAnalyzer<Object> analyzer = (ObjectAnalyzer<Object>) systemAnalyzer;
            return analyzer.sizeOf(instance, dejaVu);
        }
        ReflectAnalyzer analyzer = cachedAnalyzers.access(klass, ReflectAnalyzer::new);
        try {
            long size = analyzer.sizeOf(instance, dejaVu);
            return (size + 8 - 1) & -8;
        } catch (Exception e) {
            // Не должно произойти.
            throw new AssertionError(e);
        }
    }
}
