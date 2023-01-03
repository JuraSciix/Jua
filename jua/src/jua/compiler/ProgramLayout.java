package jua.compiler;

import jua.compiler.Tree.*;
import jua.interpreter.Address;
import jua.interpreter.InterpreterFrame;
import jua.interpreter.InterpreterThread;
import jua.interpreter.instruction.Getconst;
import jua.runtime.JuaFunction;
import jua.runtime.JuaNativeExecutor;
import jua.runtime.VirtualMachine;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.StringHeap;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public final class ProgramLayout {

    private static List<JuaFunction> builtinFunctions() {
        return Arrays.asList(
                func("print", 0, 255, ProgramLayout::nativePrint),

                func("println", 0, 255, (thread, args, argc, returnAddress) -> {
                    if (nativePrint(thread, args, argc, returnAddress)) {
                        System.out.println();
                        return true;
                    } else {
                        return false;
                    }
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
                }),

                func("sqrt", 1, 1, (thread, args, argc, returnAddress) -> {
                    Address buf = thread.getTempAddress();
                    args[0].doubleVal(buf);
                    returnAddress.set(Math.sqrt(buf.getDouble()));
                    return true;
                }),

                func("pow", 2, 2, (thread, args, argc, returnAddress) -> {
                    Address buf = thread.getTempAddress();
                    args[0].doubleVal(buf);
                    double x = buf.getDouble();
                    args[1].doubleVal(buf);
                    double y = buf.getDouble();
                    returnAddress.set(Math.pow(x, y));
                    return true;
                })
        );
    }

    private static boolean nativePrint(InterpreterThread thread, Address[] args, int argc, Address returnAddress) {
        Address tmp = thread.getTempAddress();
        PrintWriter writer = new PrintWriter(System.out);
        for (int i = 0; i < argc; i++) {
            if (!args[i].stringVal(tmp)) {
                return false;
            }
            try {
                tmp.getStringHeap().writeTo(writer);
            } catch (IOException e) {
                thread.error("Native error: " + e);
                return false;
            }
        }
        writer.flush();
        returnAddress.setNull();
        return true;
    }

    private static JuaFunction func(String name, int fromargc, int toargc, JuaNativeExecutor body) {
        return JuaFunction.fromNativeHandler(name, fromargc, toargc, body, "ProgramLayout.java");
    }

    Source mainSource;

    CompilationUnit topTree;

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

    final Lower lower = new Lower();
    final Check check = new Check();

    /**
     * Создает объект виртуальной машины из информации, собранной компилятором.
     */
    public VirtualMachine createVM() {
        throw new UnsupportedOperationException("Not yet implemented"); // todo
    }

    public Program buildProgram() {
        Gen mainCodegen = new Gen(this);

        List<Statement> toRemove = new ArrayList<>();

        topTree.code = mainCodegen.code = new Code(topTree.source);
        mainSource = topTree.source;

        List<JuaFunction> builtinFunctions = builtinFunctions();
        Map<Integer, JuaFunction> a = builtinFunctions.stream()
                .collect(Collectors.toMap(f -> addFunction(f.name()), f -> f));

        // Регистрируем встроенные (нативные) функции.
        builtinFunctions.forEach(bf -> check.functions.add(bf.name()));

        topTree.accept(lower);
        topTree.accept(check);

        List<FuncDef> funcDefs = topTree.stats.stream()
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

        List<ConstDef.Definition> constantDefs = topTree.stats.stream()
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

        topTree.stats.removeAll(toRemove);

        List<Address> constants = constantDefs.stream()
                .map(def -> {
                    Expression expr = def.expr;
                    Address address = new Address();
                    if (expr.getTag() == Tag.ARRAYLITERAL) {
                        ArrayLiteral arrayLiteral = (ArrayLiteral) expr;

                        if (!arrayLiteral.entries.isEmpty()) {
                            mainCodegen.code.addInstruction(new Getconst(tryFindConst(def.name)));
                            mainCodegen.genArrayInitializr(arrayLiteral.entries).drop();
                        }

                        address.set(new MapHeap());
//                        return new ArrayOperand();
                    } else if (expr.getTag() == Tag.LITERAL) {
                        Literal literal = (Literal) expr;
//                        return literal.type.toOperand();
                        literal.type.toOperand().writeToAddress(address);
                    } else {
                        mainSource.getLog().error(expr.pos, "Literal expected");
                        return null;
                    }
                    return address;
                })
                .collect(Collectors.toList());

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

        topTree.accept(mainCodegen);

        return new Program(mainSource, mainCodegen.code.buildCodeSegment(),
                functions.toArray(new JuaFunction[0]),
                constants.toArray(new Address[0]));
    }
}
