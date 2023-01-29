package jua.runtime;

import jua.compiler.Types;
import jua.interpreter.Address;
import jua.interpreter.AddressUtils;
import jua.interpreter.InterpreterThread;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.StringHeap;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NativeLib {

    public static Map<String, Types.Type> getNativeConstants() {
        return Collections.emptyMap();
    }

    public static List<JuaFunction> getNativeFunctions() {
        return Arrays.asList(
                func("print", 0, 255, NativeLib::nativePrint),

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
                            _args[i++] = AddressUtils.allocateCopy(a);
                        }
                    } else {
                        _args = new Address[0];
                    }
                    Thread newThread = new Thread() {
                        @Override
                        public void run() {
                            new InterpreterThread(this, thread.environment())
                                    .callAndWait(function, _args, null);
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

                func("get_stack_trace", 0, 1, (thread, args, argc, returnAddress) -> {
                    MapHeap stackTrace_jua = new MapHeap();

                    Address tmp1 = new Address();
                    Address tmp2 = new Address();

                    int limit = 0;
                    if (argc == 1) {
                        args[0].longVal(args[0]);
                        limit = (int) args[0].getLong();
                    }

                    for (StackTraceElement element : thread.getStackTrace(limit)) {
                        MapHeap element_jua = new MapHeap();
                        tmp1.set(new StringHeap("function"));
                        tmp2.set(new StringHeap(element.getFunction()));
                        element_jua.put(tmp1, tmp2);
                        tmp1.set(new StringHeap("file"));
                        tmp2.set(new StringHeap(element.getFileName()));
                        element_jua.put(tmp1, tmp2);
                        tmp1.set(new StringHeap("line"));
                        tmp2.set(new StringHeap().append(element.getLineNumber()));
                        element_jua.put(tmp1, tmp2);
                        tmp1.set(element_jua);
                        stackTrace_jua.push(tmp1);
                    }

                    returnAddress.set(stackTrace_jua);
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
                    if (argc == 2 && args[1].isValid()) {
                        _args = new Address[args[1].getMapHeap().size()];
                        int i = 0;
                        for (Address a : args[1].getMapHeap()) {
                            _args[i++] = AddressUtils.allocateCopy(a);
                        }
                    } else {
                        _args = new Address[0];
                    }
                    return thread.callAndWait(function, _args, returnAddress);
                }),

                func("sqrt", 1, 1, (thread, args, argc, returnAddress) -> {
                    Address buf = new Address();
                    args[0].doubleVal(buf);
                    returnAddress.set(Math.sqrt(buf.getDouble()));
                    return true;
                }),

                func("pow", 2, 2, (thread, args, argc, returnAddress) -> {
                    Address buf = new Address();
                    args[0].doubleVal(buf);
                    double x = buf.getDouble();
                    args[1].doubleVal(buf);
                    double y = buf.getDouble();
                    returnAddress.set(Math.pow(x, y));
                    return true;
                }),

                func("round", 1, 2, (thread, args, argc, returnAddress) -> {
                    Address buf = new Address();
                    args[0].doubleVal(buf);
                    double x = buf.getDouble();
                    if (argc == 1) {
                        returnAddress.set(Math.round(x));
                    } else {
                        args[1].longVal(buf);
                        int precision = (int) buf.getLong();
                        returnAddress.set(new BigDecimal(x).round(new MathContext(precision)).doubleValue());
                    }
                    return true;
                })
        );
    }

    private static boolean nativePrint(InterpreterThread thread, Address[] args, int argc, Address returnAddress) {
        Address tmp = new Address();
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

    @FunctionalInterface
    public interface JuaNativeExecutorLegacy {

        boolean execute(InterpreterThread thread, Address[] args, int argc, Address returnAddress);
    }

    private static JuaFunction func(String name, int fromargc, int toargc, JuaNativeExecutorLegacy body) {
        return JuaFunction.fromNativeHandler(
                name,
                fromargc,
                toargc,
                (args, argc, returnAddress) ->
                        body.execute(InterpreterThread.currentThread(), args, argc, returnAddress),
                "ProgramLayout.java"
        );
    }

}
