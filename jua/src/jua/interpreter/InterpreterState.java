package jua.interpreter;

import jua.interpreter.instructions.Instruction;
import jua.runtime.JuaFunction;
import jua.runtime.Operand;

import java.util.Map;
import java.util.Objects;

public final class InterpreterState {

    private final Instruction[] code;

    public final Operand[] stack, locals;

    private final Operand[] constantPool;

    public int cp, sp, advancedCP;

    public static final byte MSG_RUNNING = 0;

    public static final byte MSG_SENT = 1;

    public static final byte MSG_DONE = 2;

    private byte msg = MSG_RUNNING;

    private Operand returnValue;

    // todo: Ну тут и так понятно что надо сделать

    private final Map<String, Operand> constants;

    public String invokeFunctionId;

    public int invokeFunctionArgs;

    public InterpreterState(Instruction[] code,
                            int maxStack,
                            int maxLocals,
                            Operand[] constantPool,
                            Map<String, Operand> constants) {
        this.code = Objects.requireNonNull(code, "code");
        this.stack = new Operand[maxStack];
        this.locals = new Operand[maxLocals];
        this.constantPool = constantPool;
        this.constants = constants;
    }

    public Instruction[] getCode() {
        return code;
    }

    public Operand[] getStack() {
        return stack;
    }

    public Operand[] getLocals() {
        return locals;
    }

    public Operand getConstantByName(String name) {
        return constants.get(name);
    }

    public int getCP() {
        return cp;
    }

    public void setCP(int cp) {
        this.cp = cp;
    }

    public int getSP() {
        return sp;
    }

    public void setSP(int sp) {
        this.sp = sp;
    }

    public int getAdvancedCP() {
        return advancedCP;
    }

    public void setAdvancedCP(int advancedCP) {
        this.advancedCP = advancedCP;
    }

    public void pushStack(Operand operand) {
        stack[sp++] = operand;
    }

    public Operand popStack() {
        return stack[--sp];
    }

    public long popInt() {
        return getInt(popStack());
    }

    public Operand peekStack() {
        return stack[sp - 1];
    }

    public long getInt(Operand operand) {
        if (operand.canBeInt()) {
            return operand.longValue();
        }
        throw InterpreterError.inconvertibleTypes(operand.type(), Operand.Type.LONG);
    }

    public void store(int index, Operand value) {
        locals[index] = value;
    }

    public Operand load(int index) {
        return locals[index];
    }

    public byte getMsg() {
        return msg;
    }

    public void setMsg(byte msg) {
        this.msg = msg;
    }

    public Operand getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Operand returnValue) {
        this.returnValue = returnValue;
    }

    public Operand[] getConstantPool() {
        return constantPool;
    }

    public void advance() {
        cp += advancedCP;
        advancedCP = 0;
    }

    void execute(InterpreterFrame frame, InterpreterThread runtime) {
        int cp = this.cp;
        try {
            while (true) {
                cp += code[cp].run(frame.getState());
            }
        } finally {
            this.cp = cp;
        }
    }
    
    // UTIL METHODS


    public void dup1_x1() {
        stack[sp] = stack[sp - 1];
        stack[sp - 1] = stack[sp - 2];
        stack[sp - 2] = stack[sp];
        sp++;
    }

    public void dup1_x2() {
        stack[sp] = stack[sp - 1];
        stack[sp - 1] = stack[sp - 2];
        stack[sp - 2] = stack[sp - 3];
        stack[sp - 3] = stack[sp];
        sp++;
    }

    public void dup2_x1() {
        stack[sp + 1] = stack[sp - 1];
        stack[sp] = stack[sp - 2];
        stack[sp - 2] = stack[sp - 3];
        stack[sp - 3] = stack[sp + 1];
        stack[sp - 4] = stack[sp];
        sp += 2;
    }

    public void dup2_x2() {
        stack[sp + 1] = stack[sp - 1];
        stack[sp] = stack[sp - 2];
        stack[sp - 1] = stack[sp - 3];
        stack[sp - 2] = stack[sp - 4];
        stack[sp - 4] = stack[sp + 1];
        stack[sp - 5] = stack[sp];
        sp += 2;
    }
}
