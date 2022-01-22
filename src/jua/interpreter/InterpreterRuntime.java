package jua.interpreter;

import jua.Options;
import jua.interpreter.runtime.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class InterpreterRuntime {

    public static final int MAX_CALLSTACK_SIZE;

    static {
        // wtf?
        int a = Options.callStackSize();

        if (a < (1 << 10))
            a = (1 << 10);
        if (a > (Integer.MAX_VALUE >> 1))
            a = (Integer.MAX_VALUE >> 1);
        MAX_CALLSTACK_SIZE = a;
    }

    public static InterpreterRuntime copy(InterpreterRuntime env) {
        return new InterpreterRuntime(env.functions, env.constants);
    }

    // todo: Ну тут и так понятно что надо сделать

    private final Map<String, RuntimeFunction> functions;

    private final Map<String, Operand> constants;

    private final Deque<CallStackElement> callStack;

    private ProgramFrame cp;

    // todo: ну... исправить
    public ProgramFrame getFrame() {
        return cp;
    }

    public InterpreterRuntime(Map<String, RuntimeFunction> functions, Map<String, Operand> constants) {
        this.functions = functions;
        this.constants = constants;

        callStack = new ArrayDeque<>();
        callStack.add(CallStackElement.mainEntry());
    }

    public RuntimeFunction getFunctionByName(String name) {
        return functions.get(name);
    }

    public Operand getConstantByName(String name) {
        return constants.get(name);
    }

    @Deprecated
    public void enterCall(String name, Operand[] args) {
        callStack.addFirst(new CallStackElement(name, cp.sourceName(), cp.currentLine(), args, cp));

        if (callStack.size() >= MAX_CALLSTACK_SIZE) {
            throw InterpreterError.stackOverflow();
        }
    }

    public void exitCall(Operand returnValue) {
        if (callStack.isEmpty()) {
            throw new IllegalStateException("callStack is empty");
        }
//        cp.clearStack();
//        cp.clearLocals();
        cp = callStack.poll().lastFrame;
        if (cp != null) {
            cp.push(returnValue);
        }
    }

    public CallStackElement[] getCallStack() {
        return callStack.toArray(new CallStackElement[0]);
    }

    public void setProgram(ProgramFrame newCP) {
        cp = newCP;
    }

    public void run() {
        loop: while (cp != null) {
            try {
                cp.run(this);
            } catch (InterpreterError e) {
                throw new InterpreterRuntimeException(e.getMessage(), cp.sourceName(), cp.currentLine());
            } catch (Trap trap) {
                switch (trap.state()) {
                    case Trap.STATE_HALT:
                        // STATE_HALT означает полную остановку потока и
                        // выбрасывается только из обработчика операции halt
                        break loop;
                    case Trap.STATE_BTI:
                        //continue
                }
            }
        }
    }

    public String currentFile() {
        return cp.sourceName();
    }

    public int currentLine() {
        return cp.currentLine();
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
        cp.push(NullOperand.NULL);
    }

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
        cp.push(operand);
    }

    public Operand popStack() {
        return cp.pop();
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
        return cp.peek();
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
        cp.duplicate(count, x);
    }

    @Deprecated
    public void moveStack(int x) {
        cp.move(x);
    }

    public Operand getLocal(int id) {
        return cp.load(id);
    }

    public void setLocal(int id, Operand value) {
        cp.store(id, value);
    }
}
