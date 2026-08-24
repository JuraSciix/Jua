package jua.vm;

import jua.runtime.*;
import jua.runtime.StackTraceElement;
import jua.runtime.code.CodeData;
import jua.runtime.utils.Assert;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class InterpreterThread {

    private static final boolean DEBUG = false; // Информация об инструкции и стеке меж каждой инструкции

    public static final int MSG_UNSTARTED = 0; /* Поток создан, но не запущен */
    public static final int MSG_RUNNING_FRAME = 1; /* Поток выполняет фрейм */
    public static final int MSG_CALLING_FRAME = 2; /* Поток вызывает фрейм */
    public static final int MSG_POPPING_FRAME = 4; /* Поток возвращает фрейм */
    public static final int MSG_CRASHED = 6; /* В потоке произошла ошибка */
    public static final int MSG_HALTED = 7; /* Поток прерван */

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
    private final JuaEnvironment env;

    public InterpreterFrame currentFrame() {
        return current;
    }

    private Function callee;

    private int numArgs;

    private String error_msg;

    private int msg = MSG_UNSTARTED;

    private final ThreadRegion memory = new ThreadRegion();
    private final ThreadStack stack = new ThreadStack(memory);
    private final FrameFactory frameFactory = new FrameFactory();
    private InterpreterFrame current = null;

    private final ExecutionContext executionContext;

    public InterpreterThread(Thread jvmThread, JuaEnvironment environment) {
        Objects.requireNonNull(jvmThread, "JVM thread");
        Objects.requireNonNull(environment, "environment");
        bind();
        this.jvmThread = jvmThread;
        this.environment = env = environment;
        executionContext = new ExecutionContext(environment, stack(), memory());
    }

    public ThreadStack stack() {
        return stack;
    }

    @Deprecated
    public ThreadRegion memory() {
        return memory;
    }

    public ThreadRegion region() {
        return memory;
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

    private int msg() {
        return msg;
    }

    private void set_msg(int msg) {
        this.msg = msg;
    }

    public void prepareCall(Function calleeFn, int argCount) {
        if (DEBUG) {
            System.out.printf("prepareCall: name=%s%n", calleeFn.getName());
        }
//        Histogram.get().start(OPCodes._JoinFrame);
//        Histogram.get().start(OPCodes._JoinNativeFrame);
        callee = calleeFn;
        numArgs = argCount;
        set_msg(MSG_CALLING_FRAME);
    }

    public void leave() {
        doReturn();
        stack().pushGet().setNull();
    }

    public void doReturn() {
//        Histogram.get().start(OPCodes._PopFrame);
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
        ThreadRegion region = region();

        // Инициализируем системного коллера
        region.unblock(0, Math.max(1, args.length), 0); // Для передачи значений и возврата

        prepareCall(function, args.length);
        for (int i = 0; i < args.length; i++) {
            region.stackTop().set(args[args.length - 1 - i]);
            region.stackInc();
        }
        run();

        if (isCrashed()) {
            return false;
        }
        // Передаем результат
        returnAddress.set(region.stack(-1));
        return true;
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
            if (!getEnvironment()
                    .getFunctionById(frame.getFunctionId())
                    .isHidden()) {
                stackTrace.add(toStackTraceElement(frame));
            }
            frame = frame.getCaller();
            i--;
        }

        return stackTrace.toArray(new StackTraceElement[0]);
    }

    /**
     * Возвращает номер строки, которая сейчас выполняется.
     */
    int executingLineNumber(InterpreterFrame frame) {
        Function f = getEnvironment().getFunctionById(frame.getFunctionId());
        if (!f.isUserDefined()) return -1; // native function
        int cp = frame.getCP() - 1;
        return f.userCode().getLineNumberTable().getLineNumber(cp);
    }

    StackTraceElement toStackTraceElement(InterpreterFrame frame) {
        Function f = env.getFunctionById(frame.getFunctionId());
        return new StackTraceElement(f.getModule(), f.getName(), executingLineNumber(frame));
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
            } else if (!env.getFunctionById(currentFrame().getFunctionId()).isUserDefined()) {
                details = "<NATIVE>";
            } else {
                details = "CP=" + currentFrame().getCP() +
                        ", SP=" + (stack().tos());
            }
            printStackTrace();
            t.printStackTrace();
            RuntimeErrorException ex = new RuntimeErrorException("INTERPRETER CRASHED: " + details);
            ex.thread = this;
            throw ex;
        }
    }

    private void runInternal() {
        while (true) {
            switch (msg) {
                case MSG_CRASHED: {
//                    printStackTrace();
                    RuntimeErrorException ex = new RuntimeErrorException(error_msg);
                    error_msg = null;
                    ex.thread = this;
                    throw ex;
                }

                case MSG_CALLING_FRAME:
                    callFrame();
                    break;

                case MSG_POPPING_FRAME:
                    popFrame();
                    break;

                case MSG_HALTED:
                    jvmThread.interrupt();
                    return;

                case MSG_RUNNING_FRAME:
                    msg = executionContext.execute(currentFrame());
                    callee = executionContext.getMsgCallee();
                    numArgs = executionContext.getMsgArgc();
                    break;

                default:
                    Assert.error("unexpected msg: " + msg);
            }
        }
    }

    // Буфер для передачи аргументов в вызове
    private final Address[] buffer = new Address[256];
    private final Address returnAddress = new Address();

    private void callFrame() {
        ThreadRegion region = region();
        InterpreterFrame caller = currentFrame();
        Function function = callee;
        int argc = numArgs;
        int maxArgc = function.getMaxArgc();
        int minArgc = function.getMinArgc();
        Address[] defaults = function.getDefaults();

        // Копируем аргументы и опущенные значения в буфер.
        // Это временная надежная мера....
        for (int i = 0; i < argc; i++) {
            region.stackDec();
            buffer[argc - 1 - i] = region.stackTop();
        }
        if (argc < maxArgc) {
            System.arraycopy(defaults, argc - minArgc, buffer, argc, maxArgc - argc);
        }

        InterpreterFrame frame = frameFactory.allocate();
        frame.setFunctionId(function.runtimeId);
        // Сохраняем информацию о предыдущем фрейме
        frame.setCaller(caller);
        frame.setOffset(region.offset());
        frame.setTop(region.top());
        frame.stackPointer(region.stackPointer());
        // Очищаем мусорные данные
        frame.setCP(0);

        if (function.isUserDefined()) {
            // Фиксируем фрейм
            CodeData code = function.getCode();
            int registry = code.getRegNumber();
            int stack = code.getStackWide();
            region.block(registry, stack);

            // После заморозки данных сразу же устанавливаем новый фрейм.
            current = frame;

            // Отправляем данные
            for (int i = 0; i < maxArgc; i++)
                region.registry(i).set(buffer[i]);
            // Очищаем буфер
            Arrays.fill(buffer, null);

            // Мы готовы
            set_msg(MSG_RUNNING_FRAME);
        } else {
            NativeExecutor executor = function.nativeExecutor();

            // Замораживаем данные.
            // Оставляем место на стеке для возврата.
            region.block(0, 1);

            // После заморозки данных сразу же устанавливаем новый фрейм.
            current = frame;

            // Копируем часть буфера
            Address[] args = Arrays.copyOf(buffer, maxArgc);
            // Очищаем буфер
            Arrays.fill(buffer, null);

            set_msg(MSG_RUNNING_FRAME);
            boolean result;
            try {
                result = executor.execute(args, argc, returnAddress);
            } catch (Exception e) {
                String name = function.getName();
                RuntimeErrorException ex = new RuntimeErrorException(
                        "Fatal error occurred in native function " + name, e);
                ex.thread = this;
                throw ex;
            }
            if (result) {
                // Отдаём адрес возврата.
                region.stackTop().set(returnAddress);
                region.stackInc();

                popFrame();
            }
        }
    }

    private void popFrame() {
        ThreadRegion region = region();
        InterpreterFrame frame = currentFrame();

        // Снимаем адрес возврата.
        // Копирование - это временная надежная мера...
        region.stackDec();
        returnAddress.set(region.stackTop());

        // Очищаем память.
        region.clear();

        // Размораживаем данные.
        int offset = frame.offset();
        int top = frame.top();
        int stackPointer = frame.stackPointer();
        region.unblock(offset, top, stackPointer);

        // Возвращаем управление предыдущему фрейму, если есть.
        InterpreterFrame caller = frame.getCaller();
        current = caller;
        frameFactory.release();

        // Отправляем возвратное значение
        region.stackTop().set(returnAddress);
        region.stackInc();

        // Мы готовы
        set_msg(caller == null ? MSG_HALTED : MSG_RUNNING_FRAME);
    }

    public void error(String msg) {
        this.msg = MSG_CRASHED;
        executionContext.setMsg(MSG_CRASHED);
        error_msg = msg;
    }

    public void error(String fmt, Object... args) {
        error(String.format(fmt, args));
    }
}
