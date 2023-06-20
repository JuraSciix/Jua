package jua.runtime;

import jua.interpreter.Address;

public class JuaEnvironment {

    final Function[] functions;
    final ConstantMemory[] constants;

    public JuaEnvironment(Function[] functions, ConstantMemory[] constants) {
        this.functions = functions;
        this.constants = constants;
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

    public Address getConstant(int id) {
        return constants[id].address;
    }
}
