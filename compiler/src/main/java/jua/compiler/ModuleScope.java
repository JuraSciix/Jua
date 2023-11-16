package jua.compiler;

import jua.compiler.Tree.ConstDef;
import jua.compiler.Tree.FuncDef;
import jua.compiler.utils.Flow;
import jua.runtime.ConstantMemory;
import jua.runtime.interpreter.memory.Address;
import jua.runtime.interpreter.memory.AddressSupport;

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

    public static class ConstantSymbol extends VarSymbol {

        final String name;
        final int id;
        final Object value;
        final ConstDef.Definition tree;

        ConstantSymbol(String name, int id, Object value, ConstDef.Definition tree) {
            super(id);
            this.name = name;
            this.id = id;
            this.value = value;
            this.tree = tree;
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
    private final LinkedHashMap<String, ConstantSymbol> constants = new LinkedHashMap<>();
    private int constnextaddr = 0;

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

    public boolean isConstantDefined(String name) {
        return constants.containsKey(name);
    }

    public boolean isFunctionDefined(String name) {
        return functions.containsKey(name);
    }

    public ConstantSymbol defineNativeConstant(String name, Object value) {
        if (constants.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate constant: " + name);
        }
        int nextId = constnextaddr++;
        ConstantSymbol sym = new ConstantSymbol(
                name,
                nextId,
                value,
                null
        );
        constants.put(name, sym);
        return sym;
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

    public ConstantSymbol defineUserConstant(ConstDef.Definition def) {
        String name = def.name.toString();
        int nextId = constnextaddr++;
        Object type = TreeInfo.literalType(def.expr);
        ConstantSymbol sym = new ConstantSymbol(
                name,
                nextId,
                type,
                def
        );
        constants.put(name, sym);
        return sym;
    }

    public FunctionSymbol defineUserFunction(FuncDef tree, int nlocals) {
        String name = tree.name.toString();
        int nextId = funcnextaddr++;
        // The legacy code is present below
        List<String> params = new ArrayList<>();

        class ArgCountData {
            int min = Flow.count(tree.params);
            int max = 0;
        }

        ArgCountData a = Flow.reduce(tree.params, new ArgCountData(), (param, data) -> {
            params.add(param.name.toString());
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

    public ConstantSymbol defineStubConstant(String name) {
        ConstantSymbol sym = new ConstantSymbol(
                name,
                -1,
                null,
                null
        );
        constants.put(name, sym);
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

    public ConstantSymbol lookupConstant(String name) {
        return constants.get(name);
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

    public ConstantMemory[] collectConstants() {
        ConstantMemory[] constants = new ConstantMemory[this.constants.size()];
        for (ConstantSymbol sym : this.constants.values()) {
            constants[sym.id] = new ConstantMemory(sym.name, new Address());
            AddressSupport.assignObject(constants[sym.id].address, sym.value);
        }
        return constants;
    }
}
