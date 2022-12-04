package jua.compiler;

import jua.compiler.Tree.*;
import jua.interpreter.Address;
import jua.interpreter.InterpreterFrame;
import jua.interpreter.InterpreterThread;
import jua.runtime.JuaFunction;
import jua.runtime.JuaNativeExecutor;
import jua.runtime.heap.ArrayOperand;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.Operand;
import jua.runtime.heap.StringHeap;

import java.util.*;
import java.util.stream.Collectors;

public final class ProgramLayout {

    private static List<JuaFunction> builtinFunctions() {
        return Arrays.asList(
                func("print", 0, 255, (thread, args, argc, returnAddress) -> {
                    Address tmp = thread.getTempAddress();
                    for (int i = 0; i < argc; i++) {
                        if (!args[i].stringVal(tmp)) {
                            return false;
                        }
                        System.out.print(tmp.getStringHeap().toString());
                    }
                    returnAddress.setNull();
                    return true;
                }),

                func("println", 0, 255, (thread, args, argc, returnAddress) -> {
                    Address tmp = thread.getTempAddress();
                    for (int i = 0; i < argc; i++) {
                        if (!args[i].stringVal(tmp)) {
                            return false;
                        }
                        System.out.println(tmp.getStringHeap().toString());
                    }
                    returnAddress.setNull();
                    return true;
                }),

                func("ns_time", 0, 0, (thread, args, argc, returnAddress) -> {
                    returnAddress.set(System.nanoTime());
                    return true;
                }),

                func("typeof", 1, 1, (thread, args, argc, returnAddress) -> {
                    returnAddress.set(new StringHeap(args[0].getTypeName()));
                    return true;
                }),

                func("array_keys", 1, 1, (thread, args, argc, returnAddress) -> {
                    args[0].mapValue(args[0]);
                    returnAddress.set(args[0].getMapHeap().keys());
                    return true;
                }),

                func("sleep", 1, 1, (thread, args, argc, returnAddress) -> {
                    try {
                        Thread.sleep(args[0].getLong());
                        returnAddress.setNull();
                        return true;
                    } catch (InterruptedException e) {
                        thread.error("thread was interrupted");
                        return false;
                    }
                }),

                // start_thread(name, func, args)
                func("start_thread", 2, 3, (thread, args, argc, returnAddress) -> {
                    JuaFunction function = thread.environment().findFunc(args[1].getStringHeap().toString());
                    if (function == null) {
                        thread.error("function with name '%s' not exists", args[1].getStringHeap().toString());
                        return false;
                    }
                    Address[] _args;
                    if (args[2].isValid()) {
                        _args = new Address[args[2].getMapHeap().size()];
                        int i = 0;
                        for (Address a : args[2].getMapHeap()) {
                            _args[i++] = Address.allocateCopy(a);
                        }
                    } else {
                        _args = new Address[0];
                    }
                    Thread newThread = new Thread() {
                        @Override
                        public void run() {
                            new InterpreterThread(this, thread.environment())
                                    .call(function, _args, null);
                        }
                    };
                    if (!args[0].isNull()) {
                        args[0].stringVal(args[0]);
                        newThread.setName(args[0].getStringHeap().toString());
                    }
                    newThread.start();
                    returnAddress.setNull();
                    return true;
                }),

                func("current_thread_name", 0, 0, (thread, args, argc, returnAddress) -> {
                    returnAddress.set(new StringHeap(thread.getNativeThread().getName()));
                    return true;
                }),

                func("get_stack_trace", 0, 0, (thread, args, argc, returnAddress) -> {
                    MapHeap stackTraceElements = new MapHeap();

                    Address tmp1 = new Address();
                    Address tmp2 = new Address();

                    for (InterpreterFrame frame = thread.currentFrame(); frame != null; frame = frame.callingFrame()) {
                        MapHeap stackTraceElement = new MapHeap();
                        tmp1.set(new StringHeap("file"));
                        tmp2.set(new StringHeap(frame.owningFunction().filename()));
                        stackTraceElement.put(tmp1, tmp2);
                        tmp1.set(new StringHeap("line"));
                        tmp2.set(new StringHeap().append(frame.currentLineNumber()));
                        stackTraceElement.put(tmp1, tmp2);
                        tmp1.set(new StringHeap("function"));
                        tmp2.set(new StringHeap(frame.owningFunction().name()));
                        stackTraceElement.put(tmp1, tmp2);
                        tmp1.set(stackTraceElement);
                        stackTraceElements.push(tmp1);
                    }

                    returnAddress.set(stackTraceElements);
                    return true;
                }),

                func("nope", 0, 0, (thread, args, argc, returnAddress) -> {
                    returnAddress.setNull();
                    return true;
                }),

                func("invoke", 1, 2, (thread, args, argc, returnAddress) -> {
                    JuaFunction function = thread.environment().findFunc(args[0].getStringHeap().toString());
                    if (function == null) {
                        thread.error("function with name '%s' not exists", args[0].getStringHeap().toString());
                        return false;
                    }
                    Address[] _args;
                    if (args[1].isValid()) {
                        _args = new Address[args[1].getMapHeap().size()];
                        int i = 0;
                        for (Address a : args[1].getMapHeap()) {
                            _args[i++] = Address.allocateCopy(a);
                        }
                    } else {
                        _args = new Address[0];
                    }
                    return thread.call(function, _args, returnAddress);
                })

//                func("length", 1, 1, (thread, args, argc, returnAddress) -> {
//                    switch (args[0].getType()) {
//                        case ValueType.STRING:
//                            returnAddress.set(args[0].getStringHeap().length());
//                            return true;
//                        case ValueType.MAP:
//                            returnAddress.set(args[0].getMapHeap().size());
//                            return true;
//                        default:
//                            thread.error("%s has no length", args[0].getTypeName());
//                            return false;
//                    }
//                })
        );
    }

    private static JuaFunction func(String name, int fromargc, int toargc, JuaNativeExecutor body) {
        return JuaFunction.fromNativeHandler(name, fromargc, toargc, body, "ProgramLayout.java");
    }

    Source mainSource;

    Tree mainTree;

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

    public int addFunction(String name) {
        if (functionMap.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        int id = functionMap.size();
        functionMap.put(name, id);
        return id;
    }

    Lower lower;

    public Program buildProgram() {
        mainTree.accept(lower);
        Gen mainCodegen = new Gen(this);

        List<Statement> toRemove = new ArrayList<>();

        CompilationUnit top = (CompilationUnit) mainTree;
        top.code = new Code(top.source);

        mainSource = top.source;

        List<JuaFunction> builtinFunctions = builtinFunctions();
        Map<Integer, JuaFunction> a = builtinFunctions.stream()
                .collect(Collectors.toMap(f -> addFunction(f.name()), f -> f));

        List<FuncDef> funcDefs = top.stats.stream()
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
                .collect(Collectors.toList());

        List<ConstDef.Definition> constantDefs = top.stats.stream()
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
                .collect(Collectors.toList());

        top.stats.removeAll(toRemove);

        mainTree.accept(mainCodegen);


        List<JuaFunction> functions = funcDefs.stream()
                .map(fn -> {
                    fn.code = new Code(mainSource);
                    Gen codegen = new Gen(this);
                    codegen.funcSource = mainSource;
                    fn.accept(codegen);
                    return codegen.resultFunc;
                })
                .collect(Collectors.toList());

        a.forEach(functions::add);

        List<Operand> constants = constantDefs.stream()
                .map(def -> {
                    Expression expr = def.expr;
                    if (expr.getTag() == Tag.ARRAYLITERAL) {
                        ArrayLiteral arrayLiteral = (ArrayLiteral) expr;

                        if (!arrayLiteral.entries.isEmpty())
                            mainCodegen.genArrayInitializr(arrayLiteral.entries);

                        return new ArrayOperand();
                    } else if (expr.getTag() == Tag.LITERAL) {
                        Literal literal = (Literal) expr;
                        return literal.type.toOperand();
                    } else {
                        mainSource.getLog().error(expr.pos, "Literal expected");
                        return null;
                    }
                })
                .collect(Collectors.toList());

        return new Program(mainSource, mainCodegen.code.buildCodeSegment(),
                functions.toArray(new JuaFunction[0]),
                constants.toArray(new Operand[0]));
    }
}
