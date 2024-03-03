package jua.runtime;

import java.util.HashMap;
import java.util.Map;

public final class JuaEnvironment {

    private final Map<String, Function> fntab = new HashMap<>();

    public void addFunction(Function function) {
        if (function == null) {
            throw new NullPointerException("Function");
        }

        String name = function.getName();
        synchronized (fntab) {
            if (fntab.containsKey(name)) {
                throw new RuntimeErrorException("Unable to override function \"" + name + "\"");
            }
            fntab.put(name, function);
        }
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
