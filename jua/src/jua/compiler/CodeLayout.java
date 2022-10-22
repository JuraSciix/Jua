package jua.compiler;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import jua.runtime.JuaFunction;
import jua.runtime.heap.Operand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class CodeLayout {

    // todo: переместить в отдельную стадию компиляции
    final Object2IntArrayMap<String> functionNames = new Object2IntArrayMap<>();
    final ArrayList<JuaFunction> functions = new ArrayList<>();

    final Object2IntArrayMap<String> constantNames = new Object2IntArrayMap<>();
    final ArrayList<Operand> constants = new ArrayList<>();

    public final Source source;

    public CodeLayout(Source source) {
        Objects.requireNonNull(source, "Source is null");
        this.source = source;
    }

    private Code code;

    public Code getCode() throws IOException {
        if (code == null) code = new Code(source);
        return code;
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
        if (id < functions.size())
            functions.set(id, function);
        else
            functions.add(function);
    }

    public void setConstant(String name, Operand constant) {
        constantNames.putIfAbsent(name, constantNames.size());
        int id = constantNames.getInt(name);
        if (id < constants.size())
            constants.set(id, constant);
        else
            constants.add(constant);
    }
}
