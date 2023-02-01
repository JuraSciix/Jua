package jua.runtime;

import jua.interpreter.Address;
import jua.interpreter.AddressUtils;
import jua.runtime.code.CodeData;
import jua.runtime.code.ConstantPool;
import jua.utils.StringUtils;

import java.util.Arrays;

public final class Function implements ConstantPool.Entry {

    public static final long FLAG_NATIVE = 0x01; /* Нативная функция. */

    /** Интерфейс для типов, которые могут быть handle функций. */
    public interface Handle {}

    /** Название функции. */
    public final String name;

    /** Название модуля, в котором определена функция, если функция нативная, или название файла, если нет. */
    public final String module;

    /** Минимальное число принимаемых аргументов. */
    public final int minArgc;

    /** Максимальное число принимаемых аргументов. */
    public final int maxArgc;

    /** Названия параметров. */
    public final String[] params;

    /** Значения опциональных аргументов по умолчанию. */
    public final Address[] defaults;

    /** Флаги функции. */
    public final long flags;

    /** Ссылка на {@link CodeData}, если функция нативная, или {@link NativeExecutor}, если нет. */
    public final Handle handle;

    public Function(String name, String module, int minArgc, int maxArgc, String[] params, Address[] defaults, long flags, Handle handle) {
        validateFields(name, module, minArgc, maxArgc, params, defaults, flags, handle);
        this.name = name;
        this.module = module;
        this.minArgc = minArgc;
        this.maxArgc = maxArgc;
        this.params = params;
        this.defaults = defaults;
        this.flags = flags;
        this.handle = handle;
    }

    public static void validateFields(String name, String module, int minArgc, int maxArgc, String[] params, Address[] defaults, long flags, Object executor) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name should not be blank");
        }
        if (StringUtils.isBlank(module)) {
            throw new IllegalArgumentException("module should not be blank");
        }
        if (minArgc < 0) {
            throw new IllegalArgumentException("minArgc should not be negative: " + minArgc);
        }
        if (minArgc > maxArgc) {
            throw new IllegalArgumentException("minArgc should not exceed maxArgc: " + minArgc + ", " + maxArgc);
        }
        if (maxArgc > 0xff) {
            throw new IllegalArgumentException("maxArgc should not exceed 255: " + maxArgc);
        }
        if (params == null) {
            throw new IllegalArgumentException("params array should not be null");
        }
        if (params.length != maxArgc) {
            throw new IllegalArgumentException("the length of params array should be equal to maxArgc: " + params.length + ", " + maxArgc);
        }
        for (int i = 0; i < params.length; i++) {
            if (StringUtils.isBlank(params[i])) {
                throw new IllegalArgumentException("params array should not contain null values: " + Arrays.toString(params) + " at " + i);
            }
        }
        if (defaults == null) {
            throw new IllegalArgumentException("defaults array should not be null");
        }
        if (defaults.length != (maxArgc - minArgc)) {
            throw new IllegalArgumentException("the length of defaults array should be equal to the number of optional args: " + defaults.length + ", " + (maxArgc - minArgc));
        }
        for (int i = 0; i < defaults.length; i++) {
            if (AddressUtils.invalid(defaults[i])) {
                throw new IllegalArgumentException("defaults array should not contain invalid addresses: " + Arrays.toString(defaults) + " at " + i);
            }
        }
        boolean isNative = (flags & FLAG_NATIVE) != 0;
        if (isNative && !(executor instanceof NativeExecutor)) {
            throw new IllegalArgumentException("the handle of the native function must be a " + NativeExecutor.class.getName());
        }
        if (!isNative && !(executor instanceof CodeData)) {
            throw new IllegalArgumentException("the handle of the user function must be a " + CodeData.class.getName());
        }
    }

    public NativeExecutor nativeExecutor() {
        try {
            return (NativeExecutor) handle;
        } catch (ClassCastException e) {
            if ((flags & FLAG_NATIVE) == 0L) {
                throw new IllegalStateException("trying to access the native executor of a non-native function");
            } else {
                throw e;
            }
        }
    }

    public CodeData userCode() {
        try {
            return (CodeData) handle;
        } catch (ClassCastException e) {
            if ((flags & FLAG_NATIVE) != 0L) {
                throw new IllegalStateException("trying to access the user code of a native function");
            } else {
                throw e;
            }
        }
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = result * 17 + module.hashCode();
        result = result * 17 + minArgc;
        result = result * 17 + maxArgc;
        result = result * 17 + Arrays.hashCode(params);
        result = result * 17 + Arrays.hashCode(defaults);
        result = result * 17 + Long.hashCode(flags);
        result = result * 17 + handle.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Function f = (Function) o;
        // Порядок сравнений отсортирован по нарастанию сложности: от примитивов к неизвестным.
        return (minArgc == f.minArgc) &&
                (maxArgc == f.maxArgc) &&
                (flags == f.flags) &&
                name.equals(f.name) &&
                module.equals(f.module) &&
                Arrays.equals(params, f.params) &&
                Arrays.equals(defaults, f.defaults) &&
                handle.equals(f.handle);
    }

    /**
     * Преобразовывает функцию в информативный идентификатор. Например, следующий код:
     * <pre>{@code
     * fn thing(a, b = null) = {
     *     [a]: b
     * }; }</pre>
     * Будет преобразован в: <pre>{@code thing(a, b = null)}</pre>
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(name);
        buffer.append('(');
        for (int i = 0; i < maxArgc; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(params[i]);
            if (i >= minArgc) {
                buffer.append(" = ");
                buffer.append(defaults[i - minArgc].toString());
            }
        }
        buffer.append(')');
        if ((flags & FLAG_NATIVE) != 0) {
            buffer.append(" [native]");
        }
        return buffer.toString();
    }
}
