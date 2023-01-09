package jua.interpreter;

import jua.runtime.JuaEnvironment;
import jua.runtime.JuaFunction;
import jua.runtime.RuntimeErrorException;
import jua.runtime.StackTraceElement;
import jua.runtime.code.CodeSegment;
import jua.runtime.heap.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Objects;

public final class InterpreterThread {

    @Deprecated
    public interface Messages {
        byte VIRGIN = 0;
        byte RUNNING = 1;
        byte CALL = 2;
        byte RETURN = 3;
        byte FALLEN = 4;
        byte HALT = 5;
    }

    public static final byte MSG_CREATED = 0;
    public static final byte MSG_RUNNING = 1;
    public static final byte MSG_CALLING = 2;
    public static final byte MSG_POPPING = 3;
    public static final byte MSG_CRASHED = 4;
    public static final byte MSG_HALTED  = 5;

    private static final ThreadLocal<InterpreterThread> thread = new ThreadLocal<>();

    public static InterpreterThread currentThread() {
        if (thread.get() == null) {
            throw new IllegalStateException("No thread present");
        }
        return thread.get();
    }

    @Deprecated
    public static InterpreterThread getInstance() {
        return currentThread();
    }

    public static void threadError(String message) {
        currentThread().error(message);
    }

    public static void threadError(String message, Object... args) {
        currentThread().error(message, args);
    }

    @Deprecated
    public static void aError(String message) {
        threadError(message);
    }

    @Deprecated
    public static void aError(String message, Object... args) {
        threadError(message, args);
    }

    @Deprecated
    public static InterpreterThread copy(InterpreterThread thread) {
        return new InterpreterThread(thread.javaThread, thread.environment);
    }

    // todo: Ну тут и так понятно что надо сделать

    private final Thread javaThread;

    private final JuaEnvironment environment;

    private InterpreterFrame current_frame = null;

    private int calleeId;

    private int numArgs;

    private final Address tempAddress = new Address();

    public Address getTempAddress() {
        return tempAddress;
    }

    /**
     * Временное хранилище для аргументов вызовов функций.
     * Массив расширяется по мере нарастания числа аргументов за весь цикл работы программы.
     * Максимальный размер - 255. Это ограничение на число аргументов на уровне модели языка.
     */
    private Address[] argsTransfer = AddressUtils.allocateMemory(0, 0);

    private String error_msg;

    private byte msg = MSG_CREATED;

    private final Address returnAddress = new Address();

    @Deprecated
    public InterpreterFrame currentFrame() {
        return current_frame;
    }

    public InterpreterThread(Thread javaThread, JuaEnvironment environment) {
        Objects.requireNonNull(javaThread, "Java thread");
        Objects.requireNonNull(environment, "Environment");
        if (thread.get() != null) {
            throw new IllegalStateException("Thread already present");
        }
        thread.set(this);
        this.javaThread = javaThread;
        this.environment = environment;
    }

    public JuaEnvironment environment() {
        return environment;
    }

    public InterpreterFrame makeFrame(JuaFunction function) {
        InterpreterFrame sender = current_frame;
        InterpreterState state;
        if (function.isNative()) {
            // Нативные функции выполняются непосредственно на JVM.
            // У них вместо сегмента с Jua-кодом хранится нативный
            // экзекютор. Таким образом, они не нуждаются в экземпляре
            // класса InterpreterState.
            state = null;
        } else {
            CodeSegment cs = function.codeSegment();
            state = new InterpreterState(cs, this);
        }
        return new InterpreterFrame(sender, function, state);
    }

    public void joinFrame(JuaFunction callee, int argc) {
        Objects.requireNonNull(callee, "callee");

        if (((callee.maxNumArgs() - argc) | (argc - callee.minNumArgs())) < 0) {
            error((argc > callee.maxNumArgs()) ?
                    "too many arguments. (total " + callee.maxNumArgs() + ", got " + argc + ")" :
                    "too few arguments. (required " + callee.minNumArgs() + ", got " + argc + ")");
            return;
        }

        InterpreterFrame sender = current_frame;
        current_frame = makeFrame(callee);

        if (argsTransfer.length < argc) {
            // Расширяем массив без лишних аллокаций
            Address[] grownArgs = AddressUtils.allocateMemory(argc, argsTransfer.length);
            AddressUtils.arraycopy(argsTransfer, 0, grownArgs, 0, argsTransfer.length);
            argsTransfer = grownArgs;
        }
        for (int i = argc - 1; i >= 0; i--) {
            argsTransfer[i].set(sender.state().popStack());
        }

//        msg = MSG_RUNNING; // Переходим в состояние выполнения
    }

    public byte msg() {
        return msg;
    }

    // Не рекомендуется использовать
    public void set_msg(byte msg) {
        this.msg = msg;
    }

    public void returnFrame() {
        InterpreterFrame uf = current_frame;
        InterpreterFrame uf1 = uf.callingFrame();
        if (uf1 == null) {
            interrupt(); // Выполнять более нечего
            return;
        }
        current_frame = uf1;
    }

    @Deprecated
    public void enterCall(InterpreterFrame p) {

    }

    @Deprecated
    public void exitCall(Operand returnValue) {

    }

    public Thread java_thread() {
        return getNativeThread();
    }

    public Thread getNativeThread() {
        return javaThread;
    }

    public void set_callee(short calleeId, byte numArgs) {
        this.calleeId = calleeId & 0xffff;
        this.numArgs = numArgs & 0xff;
        msg = MSG_CALLING;
    }

    public Address getReturnAddress() {
        return returnAddress;
    }

    public void set_returnee() {
        this.msg = MSG_POPPING;
    }

    @Deprecated
    public void setProgram(InterpreterFrame newCP) {
        set_frame_force(newCP);
    }

    @Deprecated
    public void set_frame_force(InterpreterFrame frame) {
        current_frame = frame;
    }

    public void interrupt() {
        javaThread.interrupt();
        msg = MSG_HALTED;
        current_frame = null;
    }

    public void start() {
        set_msg(MSG_RUNNING);
    }

    public boolean isActive() {
        return javaThread.isAlive() && !javaThread.isInterrupted() && isRunning();
    }

    public boolean isRunning() {
        return msg == MSG_RUNNING;
    }

    /**
     * Вызывает указанную функцию и ждет завершения ее выполнения.
     * Возвращает {@code true}, если ошибок не произошло, иначе {@code false}.
     */
    public boolean call(JuaFunction function, Address[] args, Address returnAddress) {
        set_msg(MSG_CALLING);
        set_frame_force(makeFrame(function));
        AddressUtils.arraycopy(args, 0, argsTransfer, 0, args.length);
        numArgs = args.length;
        run();
        if (isError()) {
            return false;
        }
        if (returnAddress != null && this.returnAddress.isValid()) {
            returnAddress.set(this.returnAddress);
        }
        if (returnAddress != this.returnAddress) this.returnAddress.reset();
        return true;
    }


    public StackTraceElement[] getStackTrace() {
        return getStackTrace(0);
    }

    public StackTraceElement[] getStackTrace(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be non negative");
        }

        if (limit == 0) limit = 1024;

        ArrayList<StackTraceElement> stackTrace = new ArrayList<>(limit);

        InterpreterFrame frame = current_frame;
        int i = 0;

        while (frame != null && i < limit) {
            stackTrace.add(new StackTraceElement(
                    frame.owningFunction().name(),
                    frame.owningFunction().filename(),
                    frame.currentLineNumber()
            ));
            frame = frame.callingFrame();
            i++;
        }

        return stackTrace.toArray(new StackTraceElement[0]);
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintStream output) {
        output.printf("Stack trace for thread \"%s\" %n", javaThread.getName());
        for (StackTraceElement element : getStackTrace()) {
            output.println("\t" + element);
        }
    }

    public void run() {
        while (true) {

            switch (msg()) {

                case MSG_CRASHED: {
                    // todo: Сделать нормальный вывод ошибок
                    printStackTrace();
                    throw new RuntimeErrorException(error_msg);
                }

                case MSG_CALLING:
                case MSG_POPPING:
                    break;

                case MSG_HALTED:
                    return;

                default:
                    throw new AssertionError(msg());
            }

            InterpreterFrame frame = currentFrame();

            try {

                try {

                    if (frame.owningFunction().isNative()) {
                        switch (msg()) {
                            case MSG_CALLING:
                                set_msg(MSG_RUNNING);
                                boolean success = frame.owningFunction().nativeHandler().execute(this, argsTransfer.clone(), numArgs, returnAddress);
                                if (success && !isError()) {
                                    set_returnee();
                                }
                                break;
                            case MSG_POPPING:
                                return;
                        }
                    } else {
                        InterpreterState state = frame.state();

                        switch (msg()) {
                            case MSG_CALLING:
                                // Инициализируем стейт

                                for (int i = 0; i < numArgs; i++) {
                                    state.store(i, argsTransfer[i]);
                                }

                                for (int i = numArgs; i < frame.owningFunction().maxNumArgs(); i++) {
                                    state.constant_pool().load(
                                            frame.owningFunction().codeSegment().localTable().getLocalDefaultPCI(i),
                                            state.locals[i]
                                    );
                                }

                                calleeId = -1;
                                numArgs = 0;
                                break;

                            case MSG_POPPING:
                                state.advance();
                                if (!returnAddress.isValid()) {
                                    throw new IllegalStateException("No return value received");
                                }
                                state.pushStack(returnAddress);
                                returnAddress.reset();
                                break;
                        }

                        set_msg(MSG_RUNNING);

                        while (isRunning()) {
                            state.runDiscretely();
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
                    case MSG_RUNNING:
                        continue;

                    case MSG_CALLING: {
                        joinFrame(environment.getFunction(calleeId), numArgs);
                        break;
                    }

                    case MSG_POPPING: {
                        returnFrame();
                        break;
                    }

                    case MSG_CRASHED: {
                        // todo: Сделать нормальный вывод ошибок
                        printStackTrace();
                        throw new RuntimeErrorException(error_msg);
                    }

                    case MSG_HALTED:
                        // Поток поток подлежит завершению.
                        javaThread.interrupt();
                        return;
                }
            } catch (RuntimeErrorException e) {
                // todo: Избавиться от выброса исключения.
                e.thread = this;
                throw e;
            }
        }
    }

    @Deprecated
    public String current_location() {
        return current_frame.owningFunction().filename();
    }

    @Deprecated
    public int current_line_number() {
        return currentFrame().currentLineNumber();
    }

    @Deprecated
    public String currentFunction() {
        return currentFrame().owningFunction().name();
    }

    public void error(String msg) {
        this.msg = MSG_CRASHED;
        error_msg = msg;
    }

    public void error(String fmt, Object... args) {
        error(String.format(fmt, args));
    }

    public boolean isError() {
        return msg == MSG_CRASHED;
    }
}
