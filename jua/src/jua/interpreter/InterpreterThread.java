package jua.interpreter;

import jua.util.Options;
import jua.runtime.JuaEnvironment;
import jua.runtime.JuaFunction;
import jua.runtime.RuntimeErrorException;
import jua.runtime.code.CodeSegment;
import jua.runtime.heap.*;

import java.util.Objects;

public final class InterpreterThread {

    public interface Messages {
        byte VIRGIN = 0;
        byte RUNNING = 1;
        byte CALL = 2;
        byte RETURN = 3;
        byte FALLEN = 4;
        byte HALT = 5;
    }

    @Deprecated
    public static final int MSG_CREATED = Messages.VIRGIN;

    @Deprecated
    public static final byte MSG_RUNNING = Messages.RUNNING;

    @Deprecated
    public static final byte MSG_CALLING = Messages.CALL;

    @Deprecated
    public static final byte MSG_POPPING = Messages.RETURN;

    @Deprecated
    public static final byte MSG_CRASHED = Messages.FALLEN;

    @Deprecated
    public static final byte MSG_HALTED = Messages.HALT;

    @Deprecated
    public static final int MAX_CALLSTACK_SIZE;

    static {
        // wtf?
        int a = Options.callStackSize();

        if (a < (1 << 10)) a = (1 << 10);
        if (a > (Integer.MAX_VALUE >> 1)) a = (Integer.MAX_VALUE >> 1);
        MAX_CALLSTACK_SIZE = a;
    }

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

    public static void threadError(String message, Object... args) {
        currentThread().error(message, args);
    }

    public static void aError(String message) {
        getInstance().error(message);
    }

    public static void aError(String message, Object... args) {
        getInstance().error(message, args);
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

    // Дешевле один раз аллоцировать все, чем каждый раз понемногу
    private final Address[] argsTransfer = Address.allocateMemory(255, 0);

    private String error_msg;

    private byte msg = MSG_CREATED;

    private final Address returnAddress = new Address();

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
            state = new InterpreterState(cs.code(), cs.maxStack(), cs.maxLocals(), cs.constantPool(), this);
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
        Address.arraycopy(args, 0, argsTransfer, 0, args.length);
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

    public void run() {
        while (true) {

            switch (msg()) {

                case MSG_CRASHED: {
                    // todo: Сделать нормальный вывод ошибок
                    {
                        InterpreterFrame rf = current_frame;
                        System.err.printf("Stack trace for thread %s%n", javaThread.getName());
                        while (current_frame != null) {
                            System.err.printf("\t%s(%s:%d) %n", currentFunction(), current_location(), current_line_number());
                            current_frame = current_frame.callingFrame();
                        }
                        current_frame = rf;
                    }
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
                                    state.constant_pool().defaultLocalAt(i).writeToAddress(state.locals[i]);
                                }

                                calleeId = -1;
                                numArgs = 0;
                                break;

                            case MSG_POPPING:
                                state.advance();
                                if (returnAddress.isValid()) {
                                    state.pushStack(returnAddress);
                                    returnAddress.reset();
                                }
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
                        {
                            InterpreterFrame rf = current_frame;
                            System.err.printf("Stack trace for thread %s%n", javaThread.getName());
                            while (current_frame != null) {
                                System.err.printf("\t%s(%s:%d) %n", currentFunction(), current_location(), current_line_number());
                                current_frame = current_frame.callingFrame();
                            }
                            current_frame = rf;
                        }
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

    public String current_location() {
        return current_frame.owningFunction().filename();
    }

    public int current_line_number() {
        JuaFunction function = current_frame.owningFunction();
        if (function.isNative()) return -1;
        return function.codeSegment().lineNumberTable().getLineNumber(current_frame.state().cp());
    }

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
        return msg == Messages.FALLEN;
    }
}
