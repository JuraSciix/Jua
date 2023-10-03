package jua.compiler;

import jua.compiler.Tree.ConstDef;
import jua.compiler.Tree.FuncDef;
import jua.runtime.interpreter.memory.Address;
import jua.runtime.interpreter.memory.AddressUtils;
import jua.runtime.ConstantMemory;
import jua.runtime.Function;
import jua.runtime.VirtualMachine;
import jua.compiler.utils.JuaList;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModuleScope {

    // todo: Переместить все классы *Symbol в отдельный файл Symbols.java
    // todo: Убрать числовые идентификаторы у функций. Они должны определяться во время выполнения.

    public static class FunctionSymbol {

        public final String name;
        public final int id;
        public final int minArgc, maxArgc;
        public Object[] defs;
        public final JuaList<String> params; // null if tree is null
        public Code code;

        public Executable executable;
        public int nlocals;

        FunctionSymbol(String name, int id, int minArgc, int maxArgc, Object[] defs, JuaList<String> params) {
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
        functions.put("length", new FunctionSymbol(
                "length",
                -1,
                1,
                1,
                new Object[0],
                JuaList.of("value")
        ));
        functions.put("list", new FunctionSymbol(
                "list",
                -1,
                1,
                1,
                new Object[0],
                JuaList.of("size")
        ));
    }

    public boolean isConstantDefined(Name name) {
        return constants.containsKey(name.toString());
    }

    public boolean isFunctionDefined(Name name) {
        return functions.containsKey(name.toString());
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
                JuaList.of(params)
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
        int minargs = tree.params.count();
        int maxargs = 0;
        JuaList<String> params = new JuaList<>();
        for (FuncDef.Parameter param : tree.params) {
            params.add(param.name.toString());
            if (param.expr != null && minargs > maxargs) {
                minargs = maxargs;
            }
            maxargs++;
        }
        FunctionSymbol sym = new FunctionSymbol(
                name,
                nextId,
                minargs,
                maxargs,
                null, params
        );
        sym.nlocals = nlocals;
        functions.put(name, sym);
        return sym;
    }

    public ConstantSymbol defineStubConstant(Name name) {
        String nameString = name.toString();
        ConstantSymbol sym = new ConstantSymbol(
                nameString,
                -1,
                null,
                null
        );
        constants.put(nameString, sym);
        return sym;
    }

    public FunctionSymbol defineStubFunction(Name name) {
        String nameString = name.toString();
        FunctionSymbol sym = new FunctionSymbol(
                nameString,
                -1,
                0,
                255,
                new Object[0], null
        );
        functions.put(nameString, sym);
        return sym;
    }

    public ConstantSymbol lookupConstant(Name name) {
        return constants.get(name.toString());
    }

    public FunctionSymbol lookupFunction(Name name) {
        return functions.get(name.toString());
    }

    public Executable[] collectExecutables() {
        Executable[] executables = new Executable[funcnextaddr];
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
            AddressUtils.assignObject(constants[sym.id].address, sym.value);
        }
        return constants;
    }

    /**
     * Создает объект виртуальной машины из информации, собранной компилятором.
     */
    public VirtualMachine createVM() {
        throw new UnsupportedOperationException("Not yet implemented"); // todo
    }
}
