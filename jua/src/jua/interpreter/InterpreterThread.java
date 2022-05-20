package jua.interpreter;

import jua.Options;
import jua.runtime.*;
import jua.runtime.code.CodeSegment;

import java.util.Map;
import java.util.Objects;

public class InterpreterThread {

    @Deprecated
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

    @Deprecated
    public static InterpreterThread copy(InterpreterThread env) {
        return new InterpreterThread(env.functions, env.constants);
    }

    // todo: Ну тут и так понятно что надо сделать

    private final Map<String, JuaFunction> functions;

    private final Map<String, Operand> constants;

    private InterpreterFrame upperFrame;

    // todo: ну... исправить
    public InterpreterFrame getFrame() {
        return upperFrame;
    }

    public InterpreterThread(Map<String, JuaFunction> functions, Map<String, Operand> constants) {
        this.functions = functions;
        this.constants = constants;

    }

    public InterpreterFrame buildFrame(InterpreterFrame prev,
                                              JuaFunction function, CodeSegment program) {
        assert function == null || function.getProgram() == program;
        return new InterpreterFrame(prev,
                new InterpreterState(
                        program.getCode(),
                        program.getMaxStack(),
                        program.getMaxLocals(),
                        function.getProgram().getConstantPool(),
                        constants),
                function);
    }

    public void joinFrame(JuaFunction callee, int argc) {
        Objects.requireNonNull(callee, "callee");
        // todo: Нативных функций пока нет
        if (((callee.getMaxArgc() - argc) | (argc - callee.getMinArgc())) < 0) {
            throw new RuntimeErrorException((argc > callee.getMaxArgc()) ?
                    "arguments too many. (total " + callee.getMaxArgc() + ", got " + argc + ')' :
                    "arguments too few. (required " + callee.getMinArgc() + ", got " + argc + ')');
        }

        InterpreterFrame upperFrame1 = upperFrame;
        upperFrame = buildFrame(upperFrame1, callee, callee.getProgram());

        for (int i = callee.getMaxArgc(); i > argc; i--) upperFrame.getState().store(
                callee.getMinArgc()+(callee.getMaxArgc()-i),
                callee.getProgram().getConstantPool()[i-callee.getMinArgc()-1]);
        for (int i = argc-1; i >= 0; i--) upperFrame.getState().store(i, upperFrame1.getState().popStack());
    }

    public void returnFrame() {
        InterpreterFrame uf = upperFrame;
        Operand returnVal = uf.getState().getReturnValue();
        if (returnVal == null) throw new IllegalStateException("null return value");
        InterpreterFrame uf1 = uf.getCallerFrame();
        if (uf1 == null) {
            upperFrame = null;
            return;
        }
        // todo: Нативных функций пока нет
        uf1.getState().pushStack(returnVal);
        uf1.getState().advance();
        upperFrame = uf1;
    }

    @Deprecated
    public void enterCall(InterpreterFrame p) {

    }

    @Deprecated
    public void exitCall(Operand returnValue) {

    }

    // todo
    public void setProgram(InterpreterFrame newCP) {
        upperFrame = newCP;
    }

    public void run() {
        loop:
        while (upperFrame != null) {
            try {
                switch (upperFrame.getState().getMsg()) {
                    case InterpreterState.MSG_SENT: joinFrame(
                            functions.get(upperFrame.getState().invokeFunctionId), upperFrame.getState().invokeFunctionArgs);
                        break;
                    case
                            InterpreterState.MSG_DONE: returnFrame();
                        break;
                }

                upperFrame.execute(this);
            } catch (InterpreterError e) { // Note: на всякий случай
                RuntimeErrorException ex = new RuntimeErrorException(e.getMessage());
                ex.runtime = this;
                throw ex;
            } catch (RuntimeErrorException e) {
                // Обрабатывается в {@link jua.compiler.JuaCompiler.JuaExceptionHandler}.
                e.runtime = this;
                throw e;
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
        return upperFrame.getOwnerFunc().getProgram().getSourceName();
    }

    public int currentLine() {
        return upperFrame.getOwnerFunc().getProgram().getLineNumberTable().lineNumberOf(upperFrame.getState().getCP());
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
        upperFrame.getState().pushStack(operand);
    }

    public Operand popStack() {
        return getFrame().getState().popStack();
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
        return upperFrame.getState().peekStack();
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

    public String peekString()  {
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

    public Operand getLocal(int id) {
        Operand value = upperFrame.getState().load(id);
        if (value == null) {
            throw InterpreterError.variableNotExists(
                    upperFrame.getOwnerFunc().getProgram().getLocalNames()[id]);
        }
        return value;
    }

    public void setLocal(int id, Operand value) {
        upperFrame.getState().store(id, value);
    }
}
