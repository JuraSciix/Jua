package jua.runtime;

import jua.runtime.code.CodeSegment;

import java.net.URL;

public final class JuaFunction {

    public static JuaFunction fromCode(String name,
                                       int minNumArgs,
                                       int maxNumArgs,
                                       CodeSegment code,
                                       String filename) {
        return new JuaFunction(name, minNumArgs, maxNumArgs, code, null, filename);
    }

    public static JuaFunction fromNativeHandler(String name,
                                                int minNumArgs,
                                                int maxNumArgs,
                                                JuaNativeExecutor nativeExecutor,
                                                String filename) {
        return new JuaFunction(name, minNumArgs, maxNumArgs, null, nativeExecutor, filename);
    }


    private final String name;

    private final int minNumArgs;

    private final int maxNumArgs;

    private final CodeSegment codeSegment;

    private final JuaNativeExecutor nativeExecutor;

    private final String filename;

    private JuaFunction(String name,
                        int minNumArgs,
                        int maxNumArgs,
                        CodeSegment codeSegment,
                        JuaNativeExecutor nativeExecutor,
                        String filename) {
        this.name = name;
        this.minNumArgs = minNumArgs;
        this.maxNumArgs = maxNumArgs;
        this.codeSegment = codeSegment;
        this.nativeExecutor = nativeExecutor;
        this.filename = filename;
    }

    public String name() {
        return name;
    }

    public int minNumArgs() {
        return minNumArgs;
    }

    public int maxNumArgs() {
        return maxNumArgs;
    }

    public boolean isNative() {
        return codeSegment == null;
    }

    public CodeSegment codeSegment() {
        ensureCodeSegment();
        return codeSegment;
    }

    private void ensureCodeSegment() {
        if (codeSegment == null) {
            throw new IllegalStateException("trying access to code segment of a native function");
        }
    }

    public JuaNativeExecutor nativeHandler() {
        ensureNativeHandler();
        return nativeExecutor;
    }

    private void ensureNativeHandler() {
        if (nativeExecutor == null) {
            throw new IllegalStateException("trying access to native handler in non-native function");
        }
    }

    public String filename() {
        return filename;
    }
}
