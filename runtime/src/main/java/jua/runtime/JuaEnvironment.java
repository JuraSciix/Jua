package jua.runtime;

import java.util.*;

public final class JuaEnvironment {

    public static JuaEnvironment create(Function[] functions) {
        for (int i = 0; i < functions.length; i++) {
            functions[i].runtimeId = i;
        }
        return new JuaEnvironment(functions);
    }

    private final Map<String, Function> fntab = new HashMap<>();

    private final List<Function> functionData;

    private JuaEnvironment(Function[] functions) {
        functionData = Arrays.asList(functions);
        for (Function function : functions)
            fntab.put(function.getName(), function);
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
