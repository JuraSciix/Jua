package jua.compiler;

import jua.compiler.Tree.*;
import jua.interpreter.Address;
import jua.interpreter.instruction.Getconst;
import jua.runtime.JuaFunction;
import jua.runtime.NativeLib;
import jua.runtime.VirtualMachine;
import jua.runtime.heap.MapHeap;
import jua.util.Assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ProgramLayout {

    Source mainSource;

    CompilationUnit topTree;

    public static class FuncData {

        final String name;
        final int id;
        final int minargs, maxargs;
        final ArrayList<String> paramnames; // null if tree is null
        final FuncDef tree; // null if function is native

        FuncData(String name, int id, int minargs, int maxargs, ArrayList<String> paramnames, FuncDef tree) {
            this.name = name;
            this.id = id;
            this.minargs = minargs;
            this.maxargs = maxargs;
            this.paramnames = paramnames;
            this.tree = tree;
        }
    }

    public static class ConstData {

        final String name;
        final int id;
        final Types.Type type; // null if tree.expr isn't Tree.Literal
        final ConstDef.Definition tree;

        ConstData(String name, int id, Types.Type type, ConstDef.Definition tree) {
            this.name = name;
            this.id = id;
            this.type = type;
            this.tree = tree;
        }
    }

    private final HashMap<String, ConstData> constantMap = new HashMap<>();

    private final HashMap<String, FuncData> functionMap = new HashMap<>();

    public boolean hasConstant(Name name) {
        return constantMap.containsKey(name.value);
    }

    public FuncData tryFindFunc(Name name) {
        return functionMap.get(name.value);
    }

    public ConstData tryFindConst(Name name) {
        return constantMap.get(name.value);
    }

    public int addConstant(String name, ConstDef.Definition tree) {
        if (constantMap.containsKey(name)) {
            mainSource.getLog().error(tree.name.pos, "Constant duplicate declaration");
            return -1;
        }
        int id = constantMap.size();
        Types.Type type = null;
        Expression inner_expr = TreeInfo.stripParens(tree.expr);
        if (inner_expr.hasTag(Tag.LITERAL))
            type = ((Literal) inner_expr).type;
        constantMap.put(name, new ConstData(name, id, type, tree));
        return id;
    }

    public int addFunction(String name, FuncDef tree, JuaFunction function) {
        if (functionMap.containsKey(name)) {
            if (tree != null) {
                mainSource.getLog().error(tree.name.pos, "Function duplicate declaration");
            } else {
                mainSource.getLog().error("Native function duplicate: " + name);
            }
            return -1;
        }
        int minargs, maxargs;
        ArrayList<String> paramnames = null;
        if (tree != null) {
            minargs = tree.params.count();
            maxargs = 0;
            paramnames = new ArrayList<>();
            for (FuncDef.Parameter param : tree.params) {
                paramnames.add(param.name.value);
                if (param.expr != null && minargs > maxargs) {
                    minargs = maxargs;
                }
                maxargs++;
            }
        } else {
            minargs = function.minNumArgs();
            maxargs = function.maxNumArgs();
        }
        int id = functionMap.size();
        functionMap.put(name, new FuncData(name, id, minargs, maxargs, paramnames, tree));
        return id;
    }

    public Program buildProgram() {
        topTree.code = new Code(this, topTree.source);
        mainSource = topTree.source;

        List<JuaFunction> builtinFunctions = NativeLib.getNativeFunctions();
        Map<Integer, JuaFunction> a = builtinFunctions.stream()
                .collect(Collectors.toMap(f -> addFunction(f.name(), null, f), f -> f));

        topTree.funcDefs.stream()
                .forEach(fn -> addFunction(fn.name.value, fn, null));

        topTree.constDefs.stream()
                .flatMap(stmt -> stmt.defs.stream())
                .forEach(def -> addConstant(def.name.value, def));

        topTree.accept(topTree.code.lower);
        topTree.accept(topTree.code.check);
        topTree.accept(topTree.code.flow);
        topTree.accept(topTree.code.gen);

        List<Address> constants = topTree.constDefs.stream()
                .flatMap(constDef -> constDef.defs.stream())
                .map(def -> {
                    Expression expr = TreeInfo.stripParens(def.expr);
                    Address address = new Address();
                    if (expr.hasTag(Tag.ARRAYLITERAL)) {
                        ArrayLiteral arrayLiteral = (ArrayLiteral) expr;

                        if (!arrayLiteral.entries.isEmpty()) {
                            topTree.code.addInstruction(new Getconst(tryFindConst(def.name).id));
                            topTree.code.gen.genArrayInitializr(arrayLiteral.entries).drop();
                        }

                        address.set(new MapHeap());
//                        return new ArrayOperand();
                    } else if (expr.hasTag(Tag.LITERAL)) {
                        Literal literal = (Literal) expr;
//                        return literal.type.toOperand();
                        literal.type.toOperand().writeToAddress(address);
                    } else {
                        Assertions.error(expr.getTag());
                        return null;
                    }
                    return address;
                })
                .collect(Collectors.toList());

        List<JuaFunction> functions = topTree.funcDefs.stream()
                .map(fn -> {
                    fn.code = new Code(this, mainSource);
                    fn.accept(fn.code.lower);
                    fn.accept(fn.code.check);
                    fn.accept(fn.code.flow);
                    fn.accept(fn.code.gen);
                    return fn.code.gen.resultFunc;
                })
                .collect(Collectors.toList());

        a.forEach(functions::add);

        return new Program(mainSource, topTree.code.gen.resultFunc,
                functions.toArray(new JuaFunction[0]),
                constants.toArray(new Address[0]));
    }

    /**
     * Создает объект виртуальной машины из информации, собранной компилятором.
     */
    public VirtualMachine createVM() {
        throw new UnsupportedOperationException("Not yet implemented"); // todo
    }
}
