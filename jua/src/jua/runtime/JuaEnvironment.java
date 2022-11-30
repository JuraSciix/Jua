package jua.runtime;

import jua.runtime.heap.Operand;

import java.util.Set;

public class JuaEnvironment {

    final JuaFunction[] functions;
    final Operand[] constants;

    public JuaEnvironment(JuaFunction[] functions, Operand[] constants) {
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

    public Operand getConstant(int id) {
        return constants[id];
    }
}
