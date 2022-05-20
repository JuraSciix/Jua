package jua.runtime;

import jua.runtime.heap.Operand;

import java.util.Map;

public class JuaEnvironment {

    // todo: Произвольный доступ к функциям и константам через числовой идентификатор (условно: адрес).

    private final Map<String, JuaFunction> functions;

    private final Map<String, Operand> constants;

    public JuaEnvironment(Map<String, JuaFunction> functions, Map<String, Operand> constants) {
        this.functions = functions;
        this.constants = constants;
    }

    public JuaFunction getFunction(String name) {
        return functions.get(name);
    }

    public Operand getConstant(String name) {
        return constants.get(name);
    }
}
