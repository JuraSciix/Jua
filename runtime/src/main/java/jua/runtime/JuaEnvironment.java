package jua.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JuaEnvironment {

    private static final JuaEnvironment environment = new JuaEnvironment();

    public static JuaEnvironment getEnvironment() {
        return environment;
    }

    private final Map<String, Function> fntab = new HashMap<>();

    private final List<Function> functionData = new ArrayList<>();

    private JuaEnvironment() {
        if (environment != null) {
            throw new AssertionError();
        }
    }

    public void addFunction(Function function) {
        if (function == null) {
            throw new NullPointerException("Function");
        }
        if (function.runtimeId >= 0) {
            throw new RuntimeErrorException(
                    "Function " + function.getName() + " already have an runtime ID " + function.runtimeId);
        }

        String name = function.getName();
        synchronized (fntab) {
            if (fntab.containsKey(name)) {
                throw new RuntimeErrorException("Unable to override function \"" + name + "\"");
            }
            fntab.put(name, function);
            function.runtimeId = functionData.size();
            functionData.add(function);

        }
    }

    public Function getFunctionById(int id) {
        return functionData.get(id);
    }

    public Function lookupFunction(String name) {
        if (!fntab.containsKey(name)) {
            synchronized (fntab) {
                if (!fntab.containsKey(name)) {
                    throw new RuntimeErrorException("Function \"" + name + "\" doesnt exist");
                }
            }
        }
        return fntab.get(name);
    }
}
