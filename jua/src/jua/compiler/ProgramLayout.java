package jua.compiler;

import jua.compiler.Tree.*;
import jua.runtime.JuaFunction;
import jua.runtime.heap.ArrayOperand;
import jua.runtime.heap.Operand;

import java.util.*;

public final class ProgramLayout {

    Source mainSource;

    Tree mainTree;
    Code mainCode;

    private FuncDef[] funcDefs;
    private ConstDef.Definition[] constantDefs;

    private Operand[] constants;

    private JuaFunction[] functions;

    private final Map<String, Integer> constantMap = new HashMap<>();

    private final Map<String, Integer> functionMap = new HashMap<>();

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

    public Program buildProgram() {
        MinorGen mainCodegen = new MinorGen(this);

        List<Statement> toRemove = new ArrayList<>();

        CompilationUnit top = (CompilationUnit) mainTree;

        funcDefs = top.stats.stream()
                .filter(stmt -> stmt.hasTag(Tag.FUNCDEF))
                .map(fn -> (FuncDef) fn)
                .peek(fn -> {
                    toRemove.add(fn);
                    String fnName = fn.name.value;
                    if (functionMap.containsKey(fnName)) {
                        mainSource.getLog().error(fn.name.pos, "Function duplicate declaration");
                        return;
                    }
                    functionMap.put(fnName, functionMap.size());
                })
                .toArray(FuncDef[]::new);

        constantDefs = top.stats.stream()
                .filter(stmt -> stmt.hasTag(Tag.CONSTDEF))
                .flatMap(stmt -> {
                    toRemove.add(stmt);
                    return ((ConstDef) stmt).defs.stream();
                })
                .peek(def -> {
                    String cName = def.name.value;
                    if (constantMap.containsKey(cName)) {
                        mainSource.getLog().error(def.name.pos, "Constant duplicate declaration");
                        return;
                    }
                    constantMap.put(cName, constantMap.size());
                })
                .toArray(ConstDef.Definition[]::new);

        top.stats.removeAll(toRemove);
        top.code = mainCode;

        mainTree.accept(mainCodegen);

        functions = Arrays.stream(funcDefs)
                .map(fn -> {
                    fn.code = new Code(mainSource);
                    MinorGen codegen = new MinorGen(this);
                    codegen.funcSource = mainSource;
                    fn.accept(codegen);
                    return codegen.resultFunc;
                })
                .toArray(JuaFunction[]::new);
        constants = Arrays.stream(constantDefs)
                .map(def -> {
                    Expression expr = def.expr;
                    if (expr.getTag() == Tag.ARRAYLITERAL) {
                        ArrayLiteral arrayLiteral = (ArrayLiteral) expr;

                        if (!arrayLiteral.entries.isEmpty())
                            mainCodegen.generateArrayCreation(arrayLiteral.entries);

                        return new ArrayOperand();
                    } else if (expr.getTag() == Tag.LITERAL) {
                        Literal literal = (Literal) expr;
                        return mainCodegen.resolveOperand(literal);
                    } else {
                        mainSource.getLog().error(expr.pos, "Literal expected");
                        return null;
                    }
                })
                .toArray(Operand[]::new);

        return new Program(mainSource, mainCodegen.code.buildCodeSegment(), functions, constants);
    }
}
