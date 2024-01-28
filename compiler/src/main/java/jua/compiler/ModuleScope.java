package jua.compiler;

import jua.compiler.Tree.FuncDef;
import jua.compiler.utils.Flow;

import java.util.*;
import java.util.stream.Collectors;

public final class ModuleScope {

    // todo: Переместить все классы *Symbol в отдельный файл Symbols.java

    public static class FunctionSymbol {

        public final String name;
        public final int loargc, hiargc;
        public final String[] params;

        public Object[] defs; // У пользовательских функций изначально всегда null, потом в Gen устанавливается
        public Code code;

        public Module.Executable executable;
        public int nlocals;
        public int opcode = 0; // Не равно нулю, если языковая конструкция
        public int nativeHandle = -1; // Неотрицательно, если это заглушка для нативной функции.

        FunctionSymbol(String name, int loargc, int hiargc, String[] params, Object[] defs) {
            this.name = name;
            this.loargc = loargc;
            this.hiargc = hiargc;
            this.params = params;
            this.defs = defs;
        }
    }

    public static class VarSymbol {
        public final int id;

        VarSymbol(int id) {
            this.id = id;
        }
    }

    private final Map<String, FunctionSymbol> functions = new HashMap<>();

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
        FunctionSymbol symbol = new FunctionSymbol(name, signature.argc, signature.argc, signature.names, new Object[0]);
        symbol.opcode = opcode;
        functions.put(name, symbol);
    }

    public boolean isFunctionDefined(String name) {
        return functions.containsKey(name);
    }

    public FunctionSymbol defineNativeFunction(String name, int minArgc, int maxArgc, Object[] defs, String[] params,
                                               int nativeHandle) {
        // todo: Именованные аргументы у нативных функций
        if (functions.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate function: " + name);
        }
        FunctionSymbol sym = new FunctionSymbol(name, minArgc, maxArgc, params, defs);
        sym.nativeHandle = nativeHandle;
        functions.put(name, sym);
        return sym;
    }

    public FunctionSymbol defineUserFunction(FuncDef tree, int nlocals) {
        String name = tree.name;
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
                a.min,
                a.max,
                params.toArray(new String[0]),
                null // Потом будет установлено
        );
        sym.nlocals = nlocals;
        functions.put(name, sym);
        return sym;
    }

    public FunctionSymbol defineStubFunction(String name) {
        FunctionSymbol sym = new FunctionSymbol(
                name,
                0,
                255,
                null,
                new Object[0]
        );
        functions.put(name, sym);
        return sym;
    }

    public FunctionSymbol lookupFunction(String name) {
        return functions.get(name);
    }

    public Collection<FunctionSymbol> getUserFunctions() {
        return functions.values().stream()
                .filter(s -> s.nativeHandle < 0 && s.opcode == 0)
                .collect(Collectors.toList());
    }
}
