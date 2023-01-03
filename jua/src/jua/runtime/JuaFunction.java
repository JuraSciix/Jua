package jua.runtime;

import jua.runtime.code.CodeSegment;

import java.util.Objects;

public final class JuaFunction {

    public static final int FLAG_NATIVE = 0x1;

    public static JuaFunction fromCode(String name,
                                       int minNumArgs,
                                       int maxNumArgs,
                                       CodeSegment code,
                                       String filename) {
        return new JuaFunction(name, minNumArgs, maxNumArgs, code, filename, 0);
    }

    public static JuaFunction fromNativeHandler(String name,
                                                int minNumArgs,
                                                int maxNumArgs,
                                                JuaNativeExecutor nativeExecutor,
                                                String filename) {
        return new JuaFunction(name, minNumArgs, maxNumArgs, nativeExecutor, filename, FLAG_NATIVE);
    }

    private final String name;

    private final int lonParams;

    private final int hinParams;

    private final Object handle;

    private final String filename;

    private final int flags;

    private JuaFunction(String name,
                        int lonParams,
                        int hinParams,
                        Object handle,
                        String filename, int flags) {
        this.name = Objects.requireNonNull(name);
        this.lonParams = lonParams;
        this.hinParams = hinParams;
        this.handle = Objects.requireNonNull(handle);
        this.filename = Objects.requireNonNull(filename);
        this.flags = flags;
    }

    public String name() { return name; }

    /** Low number params */
    public int lonParams() { return lonParams; }

    /** High number params */
    public int hinParams() { return hinParams; }

    @Deprecated
    public int minNumArgs() { return lonParams(); }

    @Deprecated
    public int maxNumArgs() { return hinParams(); }

    public boolean isNative() { return (flags & FLAG_NATIVE) != 0; }

    public CodeSegment codeSegment() {
        ensureCodeSegment();
        return (CodeSegment) handle;
    }

    private void ensureCodeSegment() {
        if (isNative()) {
            throw new IllegalStateException("trying access to code segment of a native function");
        }
    }

    public JuaNativeExecutor nativeHandler() {
        ensureNativeHandler();
        return (JuaNativeExecutor) handle;
    }

    private void ensureNativeHandler() {
        if (!isNative()) {
            throw new IllegalStateException("trying access to native handler in non-native function");
        }
    }

    public String filename() { return filename; }
}
