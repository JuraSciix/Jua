package jua.runtime.interpreter;

import jua.runtime.interpreter.instruction.Instruction;
import jua.runtime.Function;
import jua.runtime.JuaEnvironment;
import jua.runtime.RuntimeErrorException;
import jua.runtime.StackTraceElement;
import jua.runtime.code.CodeData;
import jua.runtime.interpreter.memory.*;
import jua.runtime.utils.Assert;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Objects;

public final class InterpreterThread {

    private static final boolean DEBUG = false; // Информация об инструкции и стеке меж каждой инструкции

    private static final int MSG_UNSTARTED         = 0; /* Поток создан, но не запущен */
    private static final int MSG_RUNNING_FRAME     = 1; /* Поток выполняет фрейм */
    private static final int MSG_CALLING_FRAME     = 2; /* Поток вызывает фрейм */
    private static final int MSG_POPPING_FRAME     = 4; /* Поток возвращает фрейм */
    private static final int MSG_CRASHED           = 6; /* В потоке произошла ошибка */
    private static final int MSG_HALTED            = 7; /* Поток прерван */

    private static final ThreadLocal<InterpreterThread> THREADED_INSTANCE = new ThreadLocal<>();

    public static InterpreterThread currentThread() {
        InterpreterThread result = THREADED_INSTANCE.get();
        if (result == null) {
            throw new IllegalStateException("No thread was bound to the current JVM thread");
        }
        return result;
    }

    public static void threadError(String message) {
        currentThread().error(message);
    }

    public static void threadError(String message, Object... args) {
        currentThread().error(message, args);
    }

    private final Thread jvmThread;

    private final JuaEnvironment environment;

    public InterpreterFrame currentFrame() {
        return callStack.current();
    }

    private Function callee;

    private int numArgs;

    private String error_msg;

    private int msg = MSG_UNSTARTED;

    private CallStack callStack;

    private final ThreadStack stack = new ThreadStack();

    ThreadStack stack() {
        return stack;
    }

    public InterpreterThread(Thread jvmThread, JuaEnvironment environment) {
        Objects.requireNonNull(jvmThread, "JVM thread");
        Objects.requireNonNull(environment, "environment");
        bind();
        this.jvmThread = jvmThread;
        this.environment = environment;
        callStack = new ACallStack(1024,
                new AMemoryStack(300));
    }

    private void bind() {
        if (THREADED_INSTANCE.get() != null) {
            throw new IllegalStateException("Thread already present");
        }
        THREADED_INSTANCE.set(this);
    }

    public Thread getNativeThread() {
        return jvmThread;
    }

    public JuaEnvironment getEnvironment() {
        return environment;
    }

    private void enterFrame() {
        Assert.checkNonNull(callee, "callee is not set");

        callStack.push(callee);
        if ((callee.flags & Function.FLAG_NATIVE) != 0) {
            set_msg(MSG_RUNNING_FRAME);
            Address[] args = AddressUtils.allocateMemory(numArgs, 0);
            for (int i = 0; i < numArgs; i++) {
                args[numArgs - i - 1].set(stack().popGet());
            }
            boolean success = callee.nativeExecutor().execute(args, numArgs, stack().pushGet());
            if (success) {
                set_msg(MSG_POPPING_FRAME);
                callStack.pop();
                set_msg(MSG_RUNNING_FRAME);
            } else {
                Assert.check(isCrashed());
            }
        } else {
            InterpreterState state = callStack.current().getState();
            for (int i = 0; i < numArgs; i++) {
                state.getSlots().getAddress(numArgs - i - 1).set(stack().popGet());
            }
            for (int i = numArgs; i < callee.maxArgc; i++) {
                state.getSlots().getAddress(i).set(callee.defaults[i - callee.minArgc]);
            }
            set_msg(MSG_RUNNING_FRAME);
        }
    }

    private void leaveFrame() {
        stack.cleanup();
        callStack.pop();
        if (callStack.current() == null) {
            interrupt(); // Выполнять более нечего
            return;
        }
        set_msg(MSG_RUNNING_FRAME);
    }

    private int msg() {
        return msg;
    }

    private void set_msg(int msg) {
        this.msg = msg;
    }

    public void prepareCall(Function calleeFn, int argCount) {
        callee = calleeFn;
        numArgs = argCount;
        set_msg(MSG_CALLING_FRAME);
    }

    public void leave() {
        stack().pushGet().setNull();
        doReturn();
    }

    public void doReturn() {
        // Результат уже на стеке
        set_msg(MSG_POPPING_FRAME);
    }

    public void interrupt() {
        jvmThread.interrupt();
        msg = MSG_HALTED;
    }

    public boolean isActive() {
        return jvmThread.isAlive() && !jvmThread.isInterrupted() && isRunning();
    }

    public boolean isRunning() {
        return msg() == MSG_RUNNING_FRAME;
    }

    /**
     * Вызывает указанную функцию и ждет завершения ее выполнения.
     * Возвращает {@code true}, если ошибок не произошло, иначе {@code false}.
     */
    public boolean callAndWait(Function function, Address[] args, Address returnAddress) {
        set_msg(MSG_CALLING_FRAME);
        callee = function;
        if (args.length > 0) {
            // Подло подменяем адрес.. Пока что это вынужденная мера
            returnAddress.set(args[0]);
            args[0] = returnAddress;
        }
        for (Address arg : args) {
            stack().push(arg);
        }
        numArgs = args.length;
        run();
        return !isCrashed();
    }

    public StackTraceElement[] getStackTrace() {
        return getStackTrace(0);
    }

    public StackTraceElement[] getStackTrace(int limit) {
        if (limit == 0) {
            limit = 1024;
        } else if (limit < 0) {
            throw new IllegalArgumentException("Limit must be non negative");
        }

        ArrayList<StackTraceElement> stackTrace = new ArrayList<>(limit);

        InterpreterFrame frame = currentFrame();
        int i = limit;

        while (frame != null && i > 0) {
            stackTrace.add(toStackTraceElement(frame));
            frame = frame.getCaller();
            i--;
        }

        return stackTrace.toArray(new StackTraceElement[0]);
    }

    /** Возвращает номер строки, которая сейчас выполняется. */
    int executingLineNumber(InterpreterFrame frame) {
        if (!frame.getFunction().isUserDefined()) return -1; // native function
        int cp = frame.getState().getCp() - 1;
        return frame.getFunction().userCode().lineNumTable.getLineNumber(cp);
    }

    StackTraceElement toStackTraceElement(InterpreterFrame frame) {
        return new StackTraceElement(frame.getFunction().module,
                frame.getFunction().name, executingLineNumber(frame));
    }

    public void printStackTrace() {
        doPrintStackTrace(System.err, getStackTrace());
    }

    public void printStackTrace(PrintStream output) {
        doPrintStackTrace(output, getStackTrace());
    }

    public void printStackTrace(int limit) {
        doPrintStackTrace(System.err, getStackTrace(limit));
    }

    public void printStackTrace(PrintStream output, int limit) {
        doPrintStackTrace(output, getStackTrace(limit));
    }

    private void doPrintStackTrace(PrintStream output, StackTraceElement[] stackTrace) {
        Objects.requireNonNull(output, "output");
        output.printf("Stack trace for thread \"%s\":%n", jvmThread.getName());
        for (StackTraceElement element : stackTrace) {
            output.print('\t');
            element.print(output);
            output.println();
        }
    }

    public boolean isCrashed() {
        return msg() == MSG_CRASHED;
    }

    private void run() {
        try {
            runInternal();
        } catch (Throwable t) {
            String details;
            if (currentFrame() == null) {
                details = "<NO FRAME>";
            } else if ((currentFrame().getFunction().flags & Function.FLAG_NATIVE) != 0) {
                details = "<NATIVE>";
            } else {
                details = "CP=" + currentFrame().getState().getCp() +
                        ", SP=" + (stack().tos());
            }
            printStackTrace();
            t.printStackTrace();
            RuntimeErrorException ex = new RuntimeErrorException("INTERPRETER CRASHED: " + details );
            ex.thread = this;
            throw ex;
        }
    }

    private final ExecutionContext executionContext = new ExecutionContext(this);

    private void runInternal() {
        while (true) {
            if (DEBUG) {
                stack.debugUpdate(null);
            }
            switch (msg) {
                case MSG_CRASHED: {
//                    printStackTrace();
                    RuntimeErrorException ex = new RuntimeErrorException(error_msg);
                    error_msg = null;
                    ex.thread = this;
                    throw ex;
                }

                case MSG_CALLING_FRAME:
                    enterFrame();
                    continue;

                case MSG_POPPING_FRAME:
                    leaveFrame();
                    continue;

                case MSG_HALTED:
                    jvmThread.interrupt();
                    return;

                case MSG_RUNNING_FRAME:
                    break;

                default:
                    Assert.error("unexpected msg: " + msg);
            }

            CodeData codeData = currentFrame().getFunction().userCode();
            Instruction[] code = codeData.code;
            ExecutionContext context = executionContext;
            context.setState(currentFrame().getState());
            context.setConstantPool(currentFrame().getFunction().userCode().constantPool());

            while (isRunning()) {
                stack().validate();
                int cp = context.getNextCp();
                int tos = stack().tos();
                context.setNextCp(cp + 1);
                code[cp].execute(context);

                if (DEBUG) {
                    stack.debugUpdate(code[cp].getClass().getSimpleName().toLowerCase()+"{"+cp+"}");
                }
                if (msg() == MSG_CRASHED) {
                    context.setNextCp(cp);
                    stack().tos(tos);
                    break;
                }
            }
        }
    }

    public void error(String msg) {
        this.msg = MSG_CRASHED;
        error_msg = msg;
    }

    public void error(String fmt, Object... args) {
        error(String.format(fmt, args));
    }
}
