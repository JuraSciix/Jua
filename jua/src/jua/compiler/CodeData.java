package jua.compiler;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import jua.runtime.JuaFunction;
import jua.runtime.heap.Operand;

import java.net.URL;

public class CodeData {

    final String location;

    // todo: переместить в отдельную стадию компиляции
    final Object2IntArrayMap<String> functionNames = new Object2IntArrayMap<>();
    final JuaFunction[] functions = new JuaFunction[1024];

    final Object2IntArrayMap<String> constantNames = new Object2IntArrayMap<>();
    final Operand[] constants = new Operand[1024];

    public CodeData(String filename) {
        this.location = filename;
    }

    // todo: не находит некоторые функции

    public boolean testFunction(String name) {
        return functionNames.containsKey(name);
    }

    public boolean testConstant(String name) {
        return constantNames.containsKey(name);
    }

    public int functionIndex(String name) {
        // todo: Исправить костыль с вызовом несуществующей функции
        return functionNames.getOrDefault(name, -1);
    }

    public int constantIndex(String name) {
        // todo: Исправить костыль с несуществующей константой
        return constantNames.getOrDefault(name, -1);
    }

    public void setFunction(String name, JuaFunction function) {
        functionNames.putIfAbsent(name, functionNames.size());
        int id = functionNames.getInt(name);
        functions[id] = function;
    }

    public void setConstant(String name, Operand constant) {
        constantNames.putIfAbsent(name, constantNames.size());
        int id = constantNames.getInt(name);
        constants[id] = constant;
    }
}
