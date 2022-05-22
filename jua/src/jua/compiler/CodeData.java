package jua.compiler;

import jua.runtime.JuaFunction;
import jua.runtime.heap.Operand;

import java.net.URL;
import java.util.*;

public class CodeData {

    final URL location;

    // todo: переместить в отдельную стадию компиляции
    final Set<String> functionNames = new HashSet<>();
    final JuaFunction[] functions = new JuaFunction[1024];

    final Set<String> constantNames = new HashSet<>();
    final Operand[] constants = new Operand[1024];

    public CodeData(URL filename) {
        this.location = filename;
    }

    public boolean testFunction(String name) {
        return functionNames.contains(name);
    }

    public boolean testConstant(String name) {
        return constantNames.contains(name);
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

    public void setFunction(String name, JuaFunction function) {
        functionNames.add(name);
        functions[functionNames.size()-1] = function;
    }

    public void setConstant(String name, Operand constant) {
        constantNames.add(name);
        constants[constantNames.size()-1] = constant;
    }
}
