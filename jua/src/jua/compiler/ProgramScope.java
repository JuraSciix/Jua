package jua.compiler;

import jua.compiler.Tree.ConstDef;
import jua.compiler.Tree.FuncDef;
import jua.compiler.Tree.Literal;
import jua.interpreter.Address;
import jua.interpreter.AddressUtils;
import jua.runtime.Function;
import jua.runtime.NativeStdlib;
import jua.runtime.VirtualMachine;
import jua.utils.List;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public final class ProgramScope {

    // todo: Переместить все классы *Symbol в отдельный файл Symbols.java

    public static class FunctionSymbol {

        final String name;
        final int id;
        final int minArgc, maxArgc;
        final List<String> params; // null if tree is null
        Function runtimefunc;
        Code code;

        FunctionSymbol(String name, int id, int minArgc, int maxArgc, List<String> params) {
            this.name = name;
            this.id = id;
            this.minArgc = minArgc;
            this.maxArgc = maxArgc;
            this.params = params;
        }
    }

    public static class ConstantSymbol extends VarSymbol {

        final String name;
        final int id;
        final Types.Type type; // null if tree.expr isn't Tree.Literal
        final ConstDef.Definition tree;

        ConstantSymbol(String name, int id, Types.Type type, ConstDef.Definition tree) {
            super(id);
            this.name = name;
            this.id = id;
            this.type = type;
            this.tree = tree;
        }
    }

    public static class VarSymbol {

        public final int id;

        VarSymbol(int id) {
            this.id = id;
        }
    }

    private final HashMap<String, FunctionSymbol> functions = new HashMap<>();
    private int funcnextaddr = 0;
    private final HashMap<String, ConstantSymbol> constants = new HashMap<>();
    private int constnextaddr = 0;

    public ProgramScope() {
        registerOperators();
        registerNatives();
    }

    private void registerOperators() {
        functions.put("length", new FunctionSymbol(
                "length",
                -1,
                1,
                1,
                List.of("value")
        ));
        functions.put("list", new FunctionSymbol(
                "list",
                -1,
                1,
                1,
                List.of("size")
        ));
    }

    private void registerNatives() {
        NativeStdlib.getNativeConstants().forEach(this::defineNativeConstant);
        NativeStdlib.getNativeFunctions().forEach(this::defineNativeFunction);
    }

    public boolean isConstantDefined(Name name) {
        return constants.containsKey(name.toString());
    }

    public boolean isFunctionDefined(Name name) {
        return functions.containsKey(name.toString());
    }

    public ConstantSymbol defineNativeConstant(String name, Types.Type type) {
        if (constants.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate constant: " + name);
        }
        int nextId = constnextaddr++;
        ConstantSymbol sym = new ConstantSymbol(
                name,
                nextId,
                type,
                null
        );
        constants.put(name, sym);
        return sym;
    }

    public FunctionSymbol defineNativeFunction(Function function) {
        // todo: Именованные аргументы у нативных функций
        String name = function.name;
        if (functions.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate function: " + name);
        }
        int nextId = funcnextaddr++;
        FunctionSymbol sym = new FunctionSymbol(
                name,
                nextId,
                function.minArgc,
                function.maxArgc,
                List.of(function.params)
        );
        sym.runtimefunc = function;
        functions.put(name, sym);
        return sym;
    }

    public ConstantSymbol defineUserConstant(ConstDef.Definition def) {
        String name = def.name.toString();
        int nextId = constnextaddr++;
        Literal literal = (Literal) TreeInfo.stripParens(def.expr);
        ConstantSymbol sym = new ConstantSymbol(
                name,
                nextId,
                literal.type,
                def
        );
        constants.put(name, sym);
        return sym;
    }

    public FunctionSymbol defineUserFunction(FuncDef tree) {
        String name = tree.name.toString();
        int nextId = funcnextaddr++;
        // The legacy code is present below
        int minargs = tree.params.count();
        int maxargs = 0;
        List<String> params = new List<>();
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
                params
        );
        functions.put(name, sym);
        return sym;
    }

    public ConstantSymbol defineStubConstant(Name name) {
        String nameString = name.toString();
        ConstantSymbol sym = new ConstantSymbol(
                nameString,
                -1,
                Types.TYPE_NULL,
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
                null
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

    public Function[] collectFunctions() {
        Function[] functions = new Function[funcnextaddr];
        this.functions.values().forEach(symbol -> {
            if (symbol.runtimefunc != null) {
                functions[symbol.id] = symbol.runtimefunc;
            }
        });
        return functions;
    }

    public Address[] collectConstantAddresses() {
        Address[] constantAddresses = AddressUtils.allocateMemory(constants.size(), 0);
        for (ConstantSymbol constantSymbol : constants.values()) {
            constantSymbol.type.toOperand().writeToAddress(constantAddresses[constantSymbol.id]);
        }
        return constantAddresses;
    }

    /**
     * Создает объект виртуальной машины из информации, собранной компилятором.
     */
    public VirtualMachine createVM() {
        throw new UnsupportedOperationException("Not yet implemented"); // todo
    }
}