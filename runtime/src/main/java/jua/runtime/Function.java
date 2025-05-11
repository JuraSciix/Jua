package jua.runtime;

import jua.runtime.code.CodeData;
import jua.runtime.interpreter.Address;

public final class Function {

    public static final int FLAG_NATIVE = 0x01; /* Нативная функция */
    public static final int FLAG_HIDDEN = 0x02; /* Функция не показывается в трассировке стека */
    public static final int FLAG_KILLER = 0x08; /* Функция, которая точно прекращает выполнение потока1 */

    private final String name;

    private final String module;

    private final int minArgc;

    private final int maxArgc;

    private final String[] params;

    private final Address[] defaults;

    private final int flags;

    private final CodeData code;

    private final NativeExecutor nativeBody;

    public int runtimeId = -1;

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
}
