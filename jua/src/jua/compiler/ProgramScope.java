package jua.compiler;

import jua.compiler.Tree.ConstDef;
import jua.compiler.Tree.FuncDef;
import jua.compiler.Tree.Literal;
import jua.interpreter.Address;
import jua.interpreter.AddressUtils;
import jua.runtime.Function;
import jua.runtime.NativeStdlib;
import jua.runtime.VirtualMachine;

import java.util.*;

public final class ProgramScope {

    public static class FunctionSymbol {

        final String name;
        final int id;
        final int minargs, maxargs;
        final ArrayList<String> paramnames; // null if tree is null
        final FuncDef tree; // null if function is native
        Function runtimefunc;

        FunctionSymbol(String name, int id, int minargs, int maxargs, ArrayList<String> paramnames, FuncDef tree) {
            this.name = name;
            this.id = id;
            this.minargs = minargs;
            this.maxargs = maxargs;
            this.paramnames = paramnames;
            this.tree = tree;
        }
    }

    public static class ConstantSymbol {

        final String name;
        final int id;
        final Types.Type type; // null if tree.expr isn't Tree.Literal
        final ConstDef.Definition tree;

        ConstantSymbol(String name, int id, Types.Type type, ConstDef.Definition tree) {
            this.name = name;
            this.id = id;
            this.type = type;
            this.tree = tree;
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
                new ArrayList<>(Collections.singletonList("value")),
                null
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

    public void defineNativeConstant(String name, Types.Type type) {
        if (constants.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate constant: " + name);
        }
        int nextId = constnextaddr++;
        constants.put(name, new ConstantSymbol(
                name,
                nextId,
                type,
                null
        ));
    }

    public void defineNativeFunction(Function function) {
        // todo: Именованные аргументы у нативных функций
        String name = function.name;
        if (functions.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate function: " + name);
        }
        int nextId = funcnextaddr++;
        FunctionSymbol symbol = new FunctionSymbol(
                name,
                nextId,
                function.minArgc,
                function.maxArgc,
                new ArrayList<>(Arrays.asList(function.params)),
                null
        );
        symbol.runtimefunc = function;
        functions.put(name, symbol);
    }

    public void defineUserConstant(ConstDef.Definition def) {
        String name = def.name.toString();
        int nextId = constnextaddr++;
        Literal literal = (Literal) TreeInfo.stripParens(def.expr);
        constants.put(name, new ConstantSymbol(
                name,
                nextId,
                literal.type,
                def
        ));
    }

    public void defineUserFunction(FuncDef tree) {
        String name = tree.name.toString();
        int nextId = funcnextaddr++;
        // The legacy code is present below
        int minargs = tree.params.count();
        int maxargs = 0;
        ArrayList<String> paramnames = new ArrayList<>();
        for (FuncDef.Parameter param : tree.params) {
            paramnames.add(param.name.toString());
            if (param.expr != null && minargs > maxargs) {
                minargs = maxargs;
            }
            maxargs++;
        }
        functions.put(name, new FunctionSymbol(
                name,
                nextId,
                minargs,
                maxargs,
                paramnames,
                tree
        ));
    }

    public ConstantSymbol lookupConstant(Name name) {
        return constants.get(name.toString());
    }

    public FunctionSymbol lookupFunction(Name name) {
        return functions.get(name.toString());
    }

    public int countFunctions() {
        return functions.size();
    }

    public Function[] collectFunctions() {
        return functions.values().stream()
                .filter(symbol -> symbol.runtimefunc != null)
                .map(symbol -> symbol.runtimefunc)
                .toArray(Function[]::new);
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
