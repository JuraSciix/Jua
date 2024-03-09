package jua.stdlib;

import jua.runtime.*;
import jua.runtime.interpreter.Address;
import jua.runtime.interpreter.AddressSupport;
import jua.runtime.interpreter.InterpreterThread;

import java.util.ArrayList;
import java.util.List;

public class SignatureBuilder {

    public static SignatureBuilder builder() {
        return new SignatureBuilder();
    }

    private String name;

    private final List<String> params = new ArrayList<>();

    private final List<Object> defaults = new ArrayList<>();

    private boolean optionalMode = false;

    private int flags;

    private JuaCallable callable;

    private SignatureBuilder() {
        super();
    }

    public SignatureBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public SignatureBuilder param(String name) {
        if (optionalMode) {
            throw new IllegalStateException("Optional mode: required default value");
        }
        params.add(name);
        return this;
    }

    public SignatureBuilder optional() {
        optionalMode = true;
        return this;
    }
    
    public SignatureBuilder optional(String name, Object defaultValue) {
        if (!optionalMode) {
            throw new IllegalStateException("Optional mode is required");
        }
        params.add(name);
        defaults.add(defaultValue);
        return this;
    }

    public SignatureBuilder callable(JuaCallable callable) {
        this.callable = callable;
        return this;
    }

    public SignatureBuilder flags(int flags) {
        this.flags = flags;
        return this;
    }

    public Function build() {
        int required = params.size() - defaults.size();
        int total = params.size();
        String[] names = params.toArray(new String[0]);
        Address[] addrDefaults = defaults.stream()
                .map(x -> {
                    Address a = new Address();
                    AddressSupport.assignObject(a, x);
                    return a;
                })
                .toArray(Address[]::new);
        JuaCallable c = callable;
        NativeExecutor body = (args, argc, returnAddress) -> {
            Context context = new Context();
            try {
                c.call(context, args, returnAddress);
                return true;
            } catch (RuntimeErrorException e) {
                InterpreterThread.threadError(e.getMessage());
                return false;
            }
        };
        int f = flags | Function.FLAG_NATIVE;
        return new Function(name, "stdlib", required, total, names, addrDefaults, f, null, body);
    }
}
