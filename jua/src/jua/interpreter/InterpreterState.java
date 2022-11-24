package jua.interpreter;

import jua.interpreter.instruction.Instruction;
import jua.runtime.code.ConstantPool;
import jua.runtime.heap.*;

public final class InterpreterState {

    private final Instruction[] code;

    public final Operand[] stack, locals;

    private final ConstantPool constantPool;

    // todo: This fields should be typed with short
    private int cp, sp, cpAdvance;

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

    public Operand getConstantById(int id) {
        return thread.environment().getConstant(id);
    }

    public int cp() {
        return cp & 0xffff;
    }

    public void set_cp(int cp) {
        this.cp =  cp;
    }

    public int sp() {
        return sp & 0xffff;
    }

    public void set_sp(int sp) {
        this.sp =  sp;
    }

    public int cp_advance() {
        return cpAdvance & 0xffff;
    }

    public void set_cp_advance(int cpAdvance) {
        this.cpAdvance =  cpAdvance;
    }

    public void pushStack(long value) {
        pushStack(LongOperand.valueOf(value));
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
                    thread.currentFrame().owningFunction().codeSegment().localNameTable().nameOf(index) + "'.");
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

    /* ОПЕРАЦИИ НА СТЕКЕ */

    public void dup() {
        state.pushStack(state.peekStack());
    }

    public void dup2() {
        Operand a = state.popStack();
        Operand b = state.popStack();
        state.pushStack(b);
        state.pushStack(a);
        state.pushStack(b);
        state.pushStack(a);
    }

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

    public void stackAdd() {
        Operand rhs = popStack();
        Operand lhs = popStack();

        pushStack(lhs.add(rhs));
    }

    private final InterpreterState state = this;

    public void stackAnd() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.and(rhs));
    }

    public void stackClone() {
        state.pushStack(state.popStack().doClone());
    }

    public void constFalse() {
        state.pushStack(FalseOperand.FALSE);
    }

    public void constNull() {
        state.pushStack(NullOperand.NULL);
    }

    public void constTrue() {
        state.pushStack(TrueOperand.TRUE);
    }

    public void stackDec() {
        state.pushStack(state.popStack().decrement());
    }

    public void stackDiv() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.div(rhs));
    }

    public boolean stackCmpeq() {
        return popStack().equals(popStack());
    }

    public boolean stackCmpge() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (!lhs.isNumber() || !rhs.isNumber()) {
            // op inverted due to VM mechanics
            throw InterpreterError.binaryApplication("<", lhs.type(), rhs.type());
        }
        if ((lhs.isDouble() || rhs.isDouble())
                ? lhs.doubleValue() < rhs.doubleValue()
                : lhs.longValue() < rhs.longValue()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean stackCmpgt() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (!lhs.isNumber() || !rhs.isNumber()) {
            // op inverted due to VM mechanics
            throw InterpreterError.binaryApplication("<=", lhs.type(), rhs.type());
        }
        if ((lhs.isDouble() || rhs.isDouble())
                ? lhs.doubleValue() <= rhs.doubleValue()
                : lhs.longValue() <= rhs.longValue()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean stackCmple() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (!lhs.isNumber() || !rhs.isNumber()) {
            // op inverted due to VM mechanics
            throw InterpreterError.binaryApplication(">", lhs.type(), rhs.type());
        }
        if ((lhs.isDouble() || rhs.isDouble())
                ? lhs.doubleValue() > rhs.doubleValue()
                : lhs.longValue() > rhs.longValue()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean stackCmplt() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        if (!lhs.isNumber() || !rhs.isNumber()) {
            // op inverted due to VM mechanics
            throw InterpreterError.binaryApplication(">=", lhs.type(), rhs.type());
        }
        if ((lhs.isDouble() || rhs.isDouble())
                ? lhs.doubleValue() >= rhs.doubleValue()
                : lhs.longValue() >= rhs.longValue()) {
            return false;
        } else {
            return true;
        }
    }

    public void stackLength() {
        state.pushStack(new LongOperand(state.popStack().length()));
    }

    public void stackMul() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.mul(rhs));
    }

    public void stackNeg() {
        Operand val = state.popStack();
        state.pushStack(val.neg());
    }

    public void stackNewArray() {
        state.pushStack(new ArrayOperand());
    }

    public void stackNot() {
        Operand val = state.popStack();
        state.pushStack(val.not());
    }

    public void stackNsTime() {
        state.pushStack(new LongOperand(System.nanoTime()));
    }

    public void stackOr() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.or(rhs));
    }

    public void stackPos() {
        Operand val = state.peekStack();

        if (!val.isNumber())
            throw InterpreterError.unaryApplication("+", val.type());
    }

    public void stackRem() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.rem(rhs));
    }

    public void stackShl() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.shl(rhs));
    }

    public void stackShr() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.shr(rhs));
    }

    public void stackSub() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.sub(rhs));
    }

    public void stackVDec(int id) {
        Operand local = state.load(id);
        state.store(id, local.decrement());
    }

    public void stackVInc(int id) {
        Operand local = state.load(id);
        state.store(id, local.increment());
    }

    public void stackVLoad(int id) {
        Operand operand = state.load(id);
        state.pushStack(operand);
    }

    public void stackVStore(int id) {
        state.store(id, state.popStack());
    }

    public void stackXor() {
        Operand rhs = state.popStack();
        Operand lhs = state.popStack();

        state.pushStack(lhs.xor(rhs));
    }

    public void stackGettype() {
        state.pushStack(new StringOperand(state.popStack().type().name));
    }

    public void cleanupStack() {
        for (int i = stack.length - 1; i >= sp; i--)
            // todo: stack[i].reset();
            stack[i] = null;
    }

    public void stackAload() {
        Operand key = state.popStack();
        Operand map = state.popStack();
        Operand result = map.get(key);
        // todo: В новой версии языка вместо подмены должна происходит ошибка.
        state.pushStack(result == null ? NullOperand.NULL : result);
    }

    public void stackAstore() {
        Operand val = state.popStack();
        Operand key = state.popStack();
        Operand map = state.popStack();
        map.put(key, val);
    }
}
