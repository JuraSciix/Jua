package jua.runtime;

public class JuaEnvironment {

    final Function[] functions;

    public JuaEnvironment(Function[] functions) {
        this.functions = functions;
    }

    public Function findFunc(String name) {
        for (Function function : functions) {
            if (function.name.equals(name)) {
                return function;
            }
        }
        return null;
    }

    public Function getFunction(int id) {
        return functions[id];
    }
}
