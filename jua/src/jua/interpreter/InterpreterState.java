package jua.interpreter;

import jua.interpreter.instructions.Instruction;
import jua.runtime.code.ConstantPool;
import jua.runtime.heap.Operand;

public final class InterpreterState {

    private final Instruction[] code;

    public final Operand[] stack, locals;

    private final ConstantPool constantPool;

    private short cp, sp, cpAdvance;

    private final InterpreterThread thread;

    // Trusting constructor.
    InterpreterState(Instruction[] code,
                            int maxStack,
                            int maxLocals,
                            ConstantPool constantPool,
                            InterpreterThread thread) {
        this.code = code;
        this.stack = new Operand[maxStack];
        this.locals = new Operand[maxLocals];
        this.constantPool = constantPool;
        this.thread = thread;
    }

    public InterpreterThread thread() {
        return thread;
    }

    public Instruction[] code() {
        return code;
    }

    public Operand[] stack() {
        return stack;
    }

    public Operand[] locals() {
        return locals;
    }

    public Operand getConstantByName(String name) {
        return thread.environment().getConstant(name);
    }

    public int cp() {
        return cp & 0xffff;
    }

    public void set_cp(int cp) {
        this.cp = (short) cp;
    }

    public int sp() {
        return sp & 0xffff;
    }

    public void set_sp(int sp) {
        this.sp = (short) sp;
    }

    public int cp_advance() {
        return cpAdvance & 0xffff;
    }

    public void set_cp_advance(int cpAdvance) {
        this.cpAdvance = (short) cpAdvance;
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
        if (locals[index] == null) {
            thread.error("accessing an undefined variable '" +
                    thread.getFrame().function().codeSegment().localNameTable().nameOf(index) + "'.");
        }
        return locals[index];
    }

    @Deprecated
    public byte getMsg() {
        return thread.msg();
    }

    public void setMsg(byte msg) {
        thread.set_msg(msg);
    }

    public ConstantPool constant_pool() {
        return constantPool;
    }

    public void advance() {
        cp += cpAdvance;
        cpAdvance = 0;
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
