package jua.interpreter;

import jua.Options;
import jua.interpreter.instruction.Instruction;
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

    private String error_msg;

    private byte msg = MSG_CREATED;

    private Operand returnee;

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

        // todo: Нативных функций пока нет
        if (((callee.maxNumArgs() - argc) | (argc - callee.minNumArgs())) < 0) {
            error((argc > callee.maxNumArgs()) ?
                    "too many arguments. (total " + callee.maxNumArgs() + ", got " + argc + ")" :
                    "too few arguments. (required " + callee.minNumArgs() + ", got " + argc + ")");
            return;
        }

        InterpreterFrame sender = current_frame;
        current_frame = makeFrame(callee);

        for (int i = callee.maxNumArgs() - 1; i >= argc; i--) {
            current_frame.state().store(i, current_frame.state().constant_pool().defaultLocalAt(i));
        }
        for (int i = argc - 1; i >= 0; i--) {
            current_frame.state().store(i, sender.state().popStack());
        }

        msg = MSG_RUNNING; // Переходим в состояние выполнения
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
        Operand returnVal = returnee;
        if (returnVal == null) throw new IllegalStateException("null return value");
        InterpreterFrame uf1 = uf.callingFrame();
        if (uf1 == null) {
            msg = MSG_HALTED; // Выполнять больше нечего.
            current_frame = null;
            return;
        }
        // todo: Нативных функций пока нет
        uf1.state().pushStack(returnVal);
        uf1.state().advance();
        current_frame = uf1;
        msg = MSG_RUNNING; // Переходим в состояние выполнения.
    }

    @Deprecated
    public void enterCall(InterpreterFrame p) {

    }

    @Deprecated
    public void exitCall(Operand returnValue) {

    }

    public Thread java_thread() {
        return javaThread;
    }

    public void set_callee(int calleeId, int numArgs) {
        this.calleeId = calleeId;
        this.numArgs = numArgs;
        msg = MSG_CALLING;
    }

    public void set_returnee(Operand returnee) {
        this.returnee = returnee;
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

    public void run() {
        set_msg(MSG_RUNNING);

        while (true) {
            InterpreterFrame frame = current_frame;
            InterpreterState state = frame.state();
            Instruction[] code = state.code();
            int code_point = state.cp();

            try {
                state.advance();

                try {
                    while (isRunning()) {
                        code_point += code[code_point].run(state);
                    }
                } catch (Throwable t) {
                    aError("FATAL ERROR. DETAILS:\n\t" +
                            "FILE: " + current_location() + "\n\t" +
                            "LINE: " + current_line_number() + "\n\t" +
                            "CP: " + code_point + "\n\t" +
                            "SP: " + state.sp());
                }

                switch (msg) {
                    case MSG_RUNNING:
                        continue;

                    case MSG_CALLING: {
                        set_msg(MSG_RUNNING);
                        joinFrame(environment.getFunction(calleeId), numArgs);
                        calleeId = -1;
                        numArgs = 0;
                        break;
                    }

                    case MSG_POPPING: {
                        set_msg(MSG_RUNNING);
                        returnFrame();
                        break;
                    }

                    case MSG_CRASHED: {
                        // todo: Избавиться от выброса исключения.
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
            } finally {
                state.set_cp(code_point);
            }
        }
    }

    public String current_location() {
        return current_frame.owningFunction().filename();
    }

    public int current_line_number() {
        return current_frame.owningFunction().codeSegment().lineNumberTable().getLineNumber(current_frame.state().cp());
    }

    @Deprecated
    public Array getArray(Operand operand) {
        if (operand.canBeMap()) {
            return operand.arrayValue();
        }
        throw InterpreterError.inconvertibleTypes(operand.type(), Operand.Type.MAP);
    }

    public boolean getBoolean(Operand operand) {
        if (operand.canBeBoolean()) {
            return operand.booleanValue();
        }
        throw InterpreterError.inconvertibleTypes(operand.type(), Operand.Type.BOOLEAN);
    }

    public double getFloat(Operand operand) {
        if (operand.canBeFloat()) {
            return operand.doubleValue();
        }
        throw InterpreterError.inconvertibleTypes(operand.type(), Operand.Type.DOUBLE);
    }

    public long getInt(Operand operand) {
        if (operand.canBeInt()) {
            return operand.longValue();
        }
        throw InterpreterError.inconvertibleTypes(operand.type(), Operand.Type.LONG);
    }

    public String getString(Operand operand) {
        if (operand.canBeString()) {
            return operand.stringValue();
        }
        throw InterpreterError.inconvertibleTypes(operand.type(), Operand.Type.STRING);
    }

    @Deprecated
    public Operand getOperand(Array value) {
        return new ArrayOperand(value);
    }

    public Operand getOperand(boolean value) {
        return BooleanOperand.valueOf(value);
    }

    public Operand getOperand(double value) {
        return DoubleOperand.valueOf(value);
    }

    public Operand getOperand(long value) {
        return LongOperand.valueOf(value);
    }

    public Operand getOperand(String value) {
        return StringOperand.valueOf(value);
    }

    public void pushStackNull() {
        pushStack(NullOperand.NULL);
    }

    @Deprecated
    public void pushStack(Array operand) {
        pushStack(getOperand(operand));
    }

    public void pushStack(boolean operand) {
        pushStack(getOperand(operand));
    }

    public void pushStack(double operand) {
        pushStack(getOperand(operand));
    }

    public void pushStack(long operand) {
        pushStack(getOperand(operand));
    }

    public void pushStack(String operand) {
        pushStack(getOperand(operand));
    }

    public void pushStack(Operand operand) {
        current_frame.state().pushStack(operand);
    }

    public Operand popStack() {
        return currentFrame().state().popStack();
    }

    @Deprecated
    public Array popArray() {
        return getArray(popStack());
    }

    public boolean popBoolean() {
        return getBoolean(popStack());
    }

    public double popFloat() {
        return getFloat(popStack());
    }

    public long popInt() {
        return getInt(popStack());
    }

    public String popString() {
        return getString(popStack());
    }

    public Operand peekStack() {
        return current_frame.state().peekStack();
    }

    @Deprecated
    public Array peekArray() {
        return getArray(peekStack());
    }

    public boolean peekBoolean() {
        return getBoolean(peekStack());
    }

    public double peekFloat() {
        return getFloat(peekStack());
    }

    public long peekInt() {
        return getInt(peekStack());
    }

    public String peekString() {
        return getString(peekStack());
    }

    @Deprecated
    public void duplicateStack(int count, int x) {
        // does noting
    }

    @Deprecated
    public void moveStack(int x) {
        // does noting
    }

    @Deprecated
    public Operand getLocal(int id) {
        return current_frame.state().load(id);
    }

    @Deprecated
    public void setLocal(int id, Operand value) {
        current_frame.state().store(id, value);
    }

    public void error(String msg) {
        this.msg = MSG_CRASHED;
        error_msg = msg;
    }

    public void error(String fmt, Object... args) {
        error(String.format(fmt, args));
    }
}
