package jua.interpreter;

import jua.interpreter.instructions.Instruction;
import jua.runtime.Operand;

import java.util.Objects;

public final class InterpreterState {

    private final Instruction[] code;

    public final Operand[] stack, locals;

    public int cp, sp, advancedCP;

    public InterpreterState(Instruction[] code, int maxStack, int maxLocals) {
        this.code = Objects.requireNonNull(code, "code");
        this.stack = new Operand[maxStack];
        this.locals = new Operand[maxLocals];
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

    public Operand peekStack() {
        return stack[sp - 1];
    }

    public void store(int index, Operand value) {
        locals[index] = value;
    }

    public Operand load(int index) {
        return locals[index];
    }

    public void advance() {
        cp += advancedCP;
        advancedCP = 0;
    }

    void execute(InterpreterFrame frame, InterpreterRuntime runtime) {
        int cp = this.cp;
        try {
            while (true) {
                cp += code[cp].run(runtime);
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
