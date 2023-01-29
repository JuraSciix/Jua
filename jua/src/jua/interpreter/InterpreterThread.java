package jua.interpreter;

import jua.runtime.JuaEnvironment;
import jua.runtime.Function;
import jua.runtime.RuntimeErrorException;
import jua.runtime.StackTraceElement;
import jua.runtime.code.CodeData;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Objects;

public final class InterpreterThread {

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

    private InterpreterFrame executingFrame = null;

    private int calleeId;

    private int numArgs;

    @Deprecated
    private final Address tempAddress = new Address();

    private Address[] args;

    private String error_msg;

    private int msg = MSG_UNSTARTED;

    private Address returnAddress;
    private boolean checkArgc  = false;

    public InterpreterThread(Thread jvmThread, JuaEnvironment environment) {
        Objects.requireNonNull(jvmThread, "JVM thread");
        Objects.requireNonNull(environment, "environment");
        bind();
        this.jvmThread = jvmThread;
        this.environment = environment;
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

    public JuaEnvironment environment() {
        return environment;
    }

    private InterpreterFrame makeFrame(Function function, Address returnAddress) {
        InterpreterFrame sender = executingFrame;
        InterpreterState state;
        if ((function.flags & Function.FLAG_NATIVE) != 0) {
            // Нативные функции выполняются непосредственно на JVM.
            // У них вместо сегмента с Jua-кодом хранится нативный
            // экзекютор. Таким образом, они не нуждаются в экземпляре
            // класса InterpreterState.
            state = null;
        } else {
            CodeData cs = function.userCode();
            state = new InterpreterState(cs, this);
        }
        return new InterpreterFrame(sender, function, state, returnAddress);
    }

    private void joinFrame(Function callee, int argc) {
        if (checkArgc) {
            if (argc < callee.minArgc) {
                error("too few arguments: %d required, %d passed", callee.minArgc, argc);
                return;
            }
            if (argc > callee.maxArgc) {
                error("too many arguments: total %d, passed %d", callee.maxArgc, argc);
                return;
            }
            checkArgc = false;
        }

        executingFrame = makeFrame(callee, returnAddress);
    }

    private int msg() {
        return msg;
    }

    private void set_msg(int msg) {
        this.msg = msg;
    }

    private void returnFrame() {
        InterpreterFrame uf = executingFrame;
        InterpreterFrame uf1 = uf.prev();
        if (uf1 == null) {
            interrupt(); // Выполнять более нечего
            return;
        }
        executingFrame = uf1;
    }

    public void prepareCall(int functionIndex, Address[] args, int argc, Address returnAddress, boolean checkArgc) {
        calleeId = functionIndex;
        numArgs = argc;
        this.args = args;
        this.returnAddress = returnAddress;
        this.checkArgc = checkArgc;
        set_msg(MSG_CALLING_FRAME);
    }

    @Deprecated
    public Address getTempAddress() {
        return tempAddress;
    }

    public void doReturn(Address result) {
        executingFrame.returnAddress().set(result);
        set_msg(MSG_POPPING_FRAME);
    }

    public void leave() {
        executingFrame.returnAddress().setNull();
        set_msg(MSG_POPPING_FRAME);
    }

    public void interrupt() {
        jvmThread.interrupt();
        msg = MSG_HALTED;
        executingFrame = null;
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
        executingFrame = makeFrame(function, returnAddress);
        this.args = args;
        numArgs = args.length;
        this.returnAddress = returnAddress;
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

        InterpreterFrame frame = executingFrame;
        int i = limit;

        while (frame != null && i > 0) {
            stackTrace.add(frame.toStackTraceElement());
            frame = frame.prev();
            i--;
        }

        return stackTrace.toArray(new StackTraceElement[0]);
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
        output.printf("Stack trace for thread \"%s\" %n", jvmThread.getName());
        for (StackTraceElement element : stackTrace) {
            output.println("\t" + element);
        }
    }

    public boolean isCrashed() {
        return msg() == MSG_CRASHED;
    }

    private void run() {
        while (true) {

            switch (msg()) {

                case MSG_CRASHED: {
                    // todo: Сделать нормальный вывод ошибок
                    printStackTrace();
                    throw new RuntimeErrorException(error_msg);
                }

                case MSG_CALLING_FRAME:
                case MSG_POPPING_FRAME:
                    break;

                case MSG_HALTED:
                    return;

                default:
                    throw new AssertionError(msg());
            }

            InterpreterFrame frame = executingFrame;

            try {

                try {

                    if ((frame.owner().flags & Function.FLAG_NATIVE) != 0) {
                        switch (msg()) {
                            case MSG_CALLING_FRAME:
                                set_msg(MSG_RUNNING_FRAME);
                                boolean success = frame.owner().nativeExecutor().execute(args, numArgs, returnAddress);
                                if (success && !isCrashed()) {
                                    set_msg(MSG_POPPING_FRAME);
                                }
                                break;
                            case MSG_POPPING_FRAME:
                                return;
                        }
                    } else {
                        InterpreterState state = frame.state();

                        switch (msg()) {
                            case MSG_CALLING_FRAME:
                                // Инициализируем стейт

                                for (int i = 0; i < numArgs; i++) {
                                    state.store(i, args[i]);
                                }

                                for (int i = numArgs; i < frame.owner().maxArgc; i++) {
                                    state.locals[i].set(frame.owner().defaults[i - frame.owner.minArgc]);
                                }

                                calleeId = -1;
                                numArgs = 0;
                                break;

                            case MSG_POPPING_FRAME:
                                state.advance();
                                break;
                        }

                        set_msg(MSG_RUNNING_FRAME);

                        while (isRunning()) {
                            state.executeTick();
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    error("FATAL ERROR");
//                    aError("FATAL ERROR. DETAILS:\n\t" +
//                            "FILE: " + current_location() + "\n\t" +
//                            "LINE: " + current_line_number() + "\n\t" +
//                            "CP: " + state.cp() + "\n\t" +
//                            "SP: " + state.sp());
                }

                switch (msg) {
                    case MSG_RUNNING_FRAME:
                        continue;

                    case MSG_CALLING_FRAME: {
                        joinFrame(environment.getFunction(calleeId), numArgs);
                        break;
                    }

                    case MSG_POPPING_FRAME: {
                        returnFrame();
                        getTempAddress().reset();
                        break;
                    }

                    case MSG_CRASHED: {
                        // todo: Сделать нормальный вывод ошибок
                        printStackTrace();
                        throw new RuntimeErrorException(error_msg);
                    }

                    case MSG_HALTED:
                        // Поток поток подлежит завершению.
                        jvmThread.interrupt();
                        return;
                }
            } catch (RuntimeErrorException e) {
                // todo: Избавиться от выброса исключения.
                e.thread = this;
                throw e;
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
