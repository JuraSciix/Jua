package jua.runtime;

import jua.runtime.interpreter.Address;
import jua.runtime.code.CodeData;

import java.util.Arrays;
import java.util.Objects;

public final class Function {

    public static final int FLAG_NATIVE = 0x01; /* Нативная функция */
    public static final int FLAG_HIDDEN = 0x02; /* Функция не показывается в трассировке стека */
    public static final int FLAG_ONCE = 0x04; /* Функция выполняется единожды, затем возвращается только результат */
    public static final int FLAG_KILLER = 0x08; /* Функция, которая точно прекращает выполнение потока1 */

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
    public final int flags;

    public final CodeData code;

    public final NativeExecutor nativeBody;

    public Address onceContainer;
    public boolean onceCondition = false; // false=функция должна выполниться, true=только вернуть значение

    public Function(String name, String module, int minArgc, int maxArgc, String[] params, Address[] defaults, int flags, CodeData code, NativeExecutor nativeBody) {
        this.name = name;
        this.module = module;
        this.minArgc = minArgc;
        this.maxArgc = maxArgc;
        this.params = params;
        this.defaults = defaults;
        this.flags = flags;
        this.code = code;
        this.nativeBody = nativeBody;
    }

    public String getName() {
        return name;
    }

    public String getModule() {
        return module;
    }

    public int getMinArgc() {
        return minArgc;
    }

    public int getMaxArgc() {
        return maxArgc;
    }

    public String[] getParams() {
        return params;
    }

    public Address[] getDefaults() {
        return defaults;
    }

    public int getFlags() {
        return flags;
    }

    public CodeData getCode() {
        return code;
    }

    public NativeExecutor getNativeBody() {
        return nativeBody;
    }

    public boolean isUserDefined() {
        return (flags & FLAG_NATIVE) == 0;
    }

    public boolean isHidden() {
        return (flags & FLAG_HIDDEN) == FLAG_HIDDEN;
    }

    public boolean isOnce() {
        return (flags & FLAG_ONCE) == FLAG_ONCE;
    }

    public NativeExecutor nativeExecutor() {
        if ((flags & FLAG_NATIVE) == 0) {
            throw new IllegalStateException("trying to access the native executor of a non-native function");
        }
        return nativeBody;
    }

    public CodeData userCode() {
        if ((flags & FLAG_NATIVE) != 0) {
            throw new IllegalStateException("trying to access the user code of a native function");
        }
        return code;
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
        result = result * 17 + Objects.hashCode(code);
        result = result * 17 + Objects.hashCode(nativeBody);
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
                Objects.equals(code, f.code) &&
                Objects.equals(nativeBody, f.nativeBody);
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
