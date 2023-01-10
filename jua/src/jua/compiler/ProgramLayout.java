package jua.compiler;

import jua.compiler.Tree.*;
import jua.interpreter.Address;
import jua.interpreter.instruction.Getconst;
import jua.runtime.JuaFunction;
import jua.runtime.NativeLib;
import jua.runtime.VirtualMachine;
import jua.runtime.heap.MapHeap;
import jua.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ProgramLayout {

    Source mainSource;

    CompilationUnit topTree;

    public final HashMap<String, Types.Type> constantLiterals = new HashMap<>();

    private final HashMap<String, Integer> constantMap = new HashMap<>();

    private final HashMap<String, Integer> functionMap = new HashMap<>();

    public boolean hasConstant(Name name) {
        return constantMap.containsKey(name.value);
    }

    public int tryFindFunc(Name name) {
//        System.out.println(functionMap); // DEBUG
        if (name == null) {
            return -1;
        }
        return functionMap.computeIfAbsent(name.value, _name -> {
            mainSource.getLog().error(name.pos, "Trying to call an undefined function");
            return -1;
        });
    }

    public int tryFindConst(Name name) {
        return constantMap.computeIfAbsent(name.value, _name -> {
            mainSource.getLog().error(name.pos, "Trying to access an undefined constant");
            return -1;
        });
    }

    public int addFunction(String name) {
        if (functionMap.containsKey(name)) {
            throw new IllegalArgumentException(name);
        }
        int id = functionMap.size();
        functionMap.put(name, id);
        return id;
    }

    /**
     * Создает объект виртуальной машины из информации, собранной компилятором.
     */
    public VirtualMachine createVM() {
        throw new UnsupportedOperationException("Not yet implemented"); // todo
    }

    public Program buildProgram() {
        topTree.code = new Code(this, topTree.source);
        mainSource = topTree.source;

        List<JuaFunction> builtinFunctions = NativeLib.getNativeFunctions();
        Map<Integer, JuaFunction> a = builtinFunctions.stream()
                .collect(Collectors.toMap(f -> addFunction(f.name()), f -> f));

        topTree.funcDefs.stream()
                .forEach(fn -> {
                    String fnName = fn.name.value;
                    if (functionMap.containsKey(fnName)) {
                        mainSource.getLog().error(fn.name.pos, "Function duplicate declaration");
                        return;
                    }
                    functionMap.put(fnName, functionMap.size());
                });

        topTree.constDefs.stream()
                .flatMap(stmt -> stmt.defs.stream())
                .forEach(def -> {
                    String cName = def.name.value;
                    if (constantMap.containsKey(cName)) {
                        mainSource.getLog().error(def.name.pos, "Constant duplicate declaration");
                        return;
                    }
                    constantMap.put(cName, constantMap.size());
                });

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
                            topTree.code.addInstruction(new Getconst(tryFindConst(def.name)));
                            topTree.code.gen.genArrayInitializr(arrayLiteral.entries).drop();
                        }

                        address.set(new MapHeap());
//                        return new ArrayOperand();
                    } else if (expr.hasTag(Tag.LITERAL)) {
                        Literal literal = (Literal) expr;
//                        return literal.type.toOperand();
                        literal.type.toOperand().writeToAddress(address);
                    } else {
                        Assert.error(expr.getTag());
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
}
