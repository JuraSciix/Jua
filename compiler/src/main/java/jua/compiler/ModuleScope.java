package jua.compiler;

import jua.compiler.Tree.FuncDef;
import jua.compiler.utils.Flow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleScope {

    // todo: Переместить все классы *Symbol в отдельный файл Symbols.java
    // todo: Убрать числовые идентификаторы у функций. Они должны определяться во время выполнения.

    public static class FunctionSymbol {

        public final String name;
        public final int id;
        public final int minArgc, maxArgc;
        public Object[] defs;
        public final String[] params; // null if tree is null
        public Code code;

        public Module.Executable executable;
        public int nlocals;
        public int opcode;

        FunctionSymbol(String name, int id, int minArgc, int maxArgc, Object[] defs, String[] params) {
            this.name = name;
            this.id = id;
            this.minArgc = minArgc;
            this.maxArgc = maxArgc;
            this.defs = defs;
            this.params = params;
        }
    }

    public static class VarSymbol {

        public final int id;

        VarSymbol(int id) {
            this.id = id;
        }
    }

    private final LinkedHashMap<String, FunctionSymbol> functions = new LinkedHashMap<>();
    private int funcnextaddr = 0;

    public ModuleScope() {
        registerOperators();
    }

    private void registerOperators() {
        registerOperator("length", new Signature(1, "value"), InstructionUtils.OPCodes.Length);
        registerOperator("list", new Signature(1, "size"), InstructionUtils.OPCodes.NewList);
    }

    private static class Signature {
        final int argc;
        final String[] names;

        Signature(int argc, String... names) {
            this.argc = argc;
            this.names = names;
        }
    }

    private void registerOperator(String name, Signature signature, int opcode) {
        FunctionSymbol symbol = new FunctionSymbol(name, -1, signature.argc, signature.argc, new Object[0], signature.names);
        symbol.opcode = opcode;
        functions.put(name, symbol);
    }


    public boolean isFunctionDefined(String name) {
        return functions.containsKey(name);
    }

    public FunctionSymbol defineNativeFunction(String name, int minArgc, int maxArgc, Object[] defs, String[] params) {
        // todo: Именованные аргументы у нативных функций
        if (functions.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate function: " + name);
        }
        int nextId = funcnextaddr++;
        FunctionSymbol sym = new FunctionSymbol(
                name,
                nextId,
                minArgc,
                maxArgc,
                defs,
                params
        );
        functions.put(name, sym);
        return sym;
    }

    public FunctionSymbol defineUserFunction(FuncDef tree, int nlocals) {
        String name = tree.name;
        int nextId = funcnextaddr++;
        // The legacy code is present below
        List<String> params = new ArrayList<>();

        class ArgCountData {
            int min = Flow.count(tree.params);
            int max = 0;
        }

        ArgCountData a = Flow.reduce(tree.params, new ArgCountData(), (param, data) -> {
            params.add(param.name);
            if (param.expr != null && data.min > data.max) {
                data.min = data.max;
            }
            data.max++;
            return data;
        });
        FunctionSymbol sym = new FunctionSymbol(
                name,
                nextId,
                a.min,
                a.max,
                null,
                params.toArray(new String[0])
        );
        sym.nlocals = nlocals;
        functions.put(name, sym);
        return sym;
    }

    public FunctionSymbol defineStubFunction(String name) {
        FunctionSymbol sym = new FunctionSymbol(
                name,
                -1,
                0,
                255,
                new Object[0], null
        );
        functions.put(name, sym);
        return sym;
    }

    public FunctionSymbol lookupFunction(String name) {
        return functions.get(name);
    }

    public Module.Executable[] collectExecutables() {
        Module.Executable[] executables = new Module.Executable[funcnextaddr];
        this.functions.values().forEach(symbol -> {
            if (symbol.id < 0) return;
            executables[symbol.id] = symbol.executable;
        });
        return executables;
    }

    public String[] functionNames() {
        return functions.entrySet().stream()
                .filter(e -> e.getValue().id >= 0)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }
}
