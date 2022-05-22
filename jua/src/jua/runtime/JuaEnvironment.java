package jua.runtime;

import jua.runtime.heap.Operand;

import java.util.Set;

public class JuaEnvironment {

    // todo: Произвольный доступ к функциям и константам через числовой идентификатор (условно: адрес).
    final Set<String> functionNames;
    final JuaFunction[] functions;

    final Set<String> constantNames;
    final Operand[] constants;

    public JuaEnvironment(Set<String> functionNames, JuaFunction[] functions, Set<String> constantNames, Operand[] constants) {
        this.functionNames = functionNames;
        this.functions = functions;
        this.constantNames = constantNames;
        this.constants = constants;
    }

    public int functionIndex(String name) {
        int index = 0;
        for (String entry : functionNames) {
            if (entry.equals(name)) break;
            index++;
        }
        return index;
    }

    public int constantIndex(String name) {
        int index = 0;
        for (String entry : constantNames) {
            if (entry.equals(name)) break;
            index++;
        }
        return index;
    }

    public JuaFunction getFunction(String name) {
        return functions[functionIndex(name)];
    }

    public JuaFunction getFunction(int id) {
        return functions[id];
    }

    public Operand getConstant(String name) {
        return constants[constantIndex(name)];
    }

    public Operand getOperand(int id) {
        return constants[id];
    }
}
