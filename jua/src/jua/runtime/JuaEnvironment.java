package jua.runtime;

import jua.interpreter.Address;

public class JuaEnvironment {

    final JuaFunction[] functions;
    final Address[] constants;

    public JuaEnvironment(JuaFunction[] functions, Address[] constants) {
        this.functions = functions;
        this.constants = constants;
    }

    public JuaFunction findFunc(String name) {
        for (JuaFunction function : functions) {
            if (function.name().equals(name)) {
                return function;
            }
        }
        return null;
    }

    public JuaFunction getFunction(int id) {
        return functions[id];
    }

    public Address getConstant(int id) {
        return constants[id];
    }
}
