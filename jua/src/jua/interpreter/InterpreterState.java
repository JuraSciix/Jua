package jua.interpreter;

import jua.interpreter.instruction.Instruction;
import jua.runtime.ValueType;
import jua.runtime.code.ConstantPool;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.Operand;
import jua.runtime.heap.StringHeap;

public final class InterpreterState {

    private final Instruction[] code;

    public final Address[] stack, locals;

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
        this.stack = Address.allocateMemory(0, maxStack);
        this.locals = Address.allocateMemory(0, maxLocals);
        this.constantPool = constantPool;
        this.thread = thread;
    }

    public InterpreterThread thread() {
        return thread;
    }

    public Instruction[] code() {
        return code;
    }

    public void getconst(int id) {
        thread.environment().getConstant(id).writeToAddress(peekStack());
        sp++;
    }

    @Deprecated
    public Address getConstantById(int id) {
        throw new UnsupportedOperationException();
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
        top().set(value);
    }

    @Deprecated
    public void pushStack(Operand value) {
        value.writeToAddress(top());
    }

    public void pushStack(Address address) {
        stack[sp++].set(address);
    }

    public Address popStack() {
        return stack[--sp];
    }

    public long popInt() {
        return getInt(popStack());
    }

    public Address peekStack() {
        return stack[sp - 1];
    }

    public long getInt(Address a) {
        return a.longVal();
    }

    @Deprecated
    public void store(int index, Operand value) {
        value.writeToAddress(locals[index]);
    }

    public void store(int index, Address value) {
        locals[index].set(value);
    }

    public void load(int index) {
        if (locals[index] == null) {
            thread.error("accessing an undefined variable '" +
                    thread.currentFrame().owningFunction().codeSegment().localNameTable().nameOf(index) + "'.");
        }
        peekStack().set(locals[index]);
        sp++;
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
        Address peek = peekStack();
        top().set(peek);
    }

    public Address top() {
        return stack[sp++];
    }

    public void dup2() {
        Address a = popStack();
        Address b = popStack();
        pushStack(b);
        pushStack(a);
        pushStack(b);
        pushStack(a);
    }

    public void dup1_x1() {
        stack[sp].set(stack[sp - 1]);
        stack[sp - 1].set(stack[sp - 2]);
        stack[sp - 2].set(stack[sp]);
        sp++;
    }

    public void dup1_x2() {
        stack[sp].set(stack[sp - 1]);
        stack[sp - 1].set(stack[sp - 2]);
        stack[sp - 2].set(stack[sp - 3]);
        stack[sp - 3].set(stack[sp]);
        sp++;
    }

    public void dup2_x1() {
        stack[sp + 1].set(stack[sp - 1]);
        stack[sp].set(stack[sp - 2]);
        stack[sp - 2].set(stack[sp - 3]);
        stack[sp - 3].set(stack[sp + 1]);
        stack[sp - 4].set(stack[sp]);
        sp += 2;
    }

    public void dup2_x2() {
        stack[sp + 1].set(stack[sp - 1]);
        stack[sp].set(stack[sp - 2]);
        stack[sp - 1].set(stack[sp - 3]);
        stack[sp - 2].set(stack[sp - 4]);
        stack[sp - 4].set(stack[sp + 1]);
        stack[sp - 5].set(stack[sp]);
        sp += 2;
    }

    public void stackAdd() {
        lhs().add(rhs(), lhs());
        sp--;
    }

    private Address lhs() { return stack[sp - 2]; }
    private Address rhs() { return stack[sp - 1]; }

    public void stackAnd() {
        lhs().and(rhs(), lhs());
        sp--;
    }

    @Deprecated
    public void stackClone() {
        
    }

    public void constFalse() {
        top().set(false);
    }

    public void constNull() {
        top().setNull();
    }

    public void constTrue() {
        top().set(true);
    }

    public void stackInc() {
        peekStack().inc(peekStack());
    }

    public void stackDec() {
        peekStack().dec(peekStack());
    }

    public void stackDiv() {
        lhs().div(rhs(), lhs());
        sp--;
    }

    public boolean stackCmpeq() {
        int cmp = lhs().compare(rhs(), 1);
        sp -= 2;
        return cmp == 0;
    }

    public boolean stackCmpge() {
        int cmp = lhs().compare(rhs(), -1);
        sp -= 2;
        return cmp >= 0;
    }

    public boolean stackCmpgt() {
        int cmp = lhs().compare(rhs(), -1);
        sp -= 2;
        return cmp > 0;
    }

    public boolean stackCmple() {
        int cmp = lhs().compare(rhs(), 1);
        sp -= 2;
        return cmp <= 0;
    }

    public boolean stackCmplt() {
        int cmp = lhs().compare(rhs(), 1);
        sp -= 2;
        return cmp < 0;
    }

    public void stackLength() {
        switch (peekStack().typeCode()) {
            case ValueType.STRING:
                pushStack(popStack().stringVal().length());
                break;
            case ValueType.MAP:
                pushStack(popStack().mapValue().size());
                break;
            default:
                thread.error("Invalid length");
        }
    }

    public void stackMul() {
       lhs().mul(rhs(), lhs());
       sp--;
    }

    public void stackNeg() {
        peekStack().neg(peekStack());
    }

    public void stackNewArray() {
       top().set(new MapHeap());
    }

    public void stackNot() {
       peekStack().not(peekStack());
    }

    public void stackNanosTime() {
        pushStack(System.nanoTime());
    }

    public void stackOr() {
        lhs().or(rhs(), lhs());
        sp--;
    }

    public void stackPos() {
       peekStack().pos(peekStack());
    }

    public void stackRem() {
        lhs().rem(rhs(), lhs());
        sp--;
    }

    public void stackShl() {
        lhs().shl(rhs(), lhs());
        sp--;
    }

    public void stackShr() {
        lhs().shr(rhs(), lhs());
        sp--;
    }

    public void stackSub() {
        lhs().sub(rhs(), lhs());
        sp--;
    }

    public void stackVDec(int id) {
        testLocal(id);
        locals[id].dec(locals[id]);
    }

    public void stackVInc(int id) {
        testLocal(id);
        locals[id].inc(locals[id]);
    }

    public void stackVLoad(int id) {
        testLocal(id);
        pushStack(locals[id]);
    }

    public void stackVStore(int id) {
        locals[id].set(popStack());
    }

    private void testLocal(int id) {
        if (locals[id].typeCode() == ValueType.UNDEFINED) {
            thread.error("Access to undefined variable");
        }
    }

    public void stackXor() {
       lhs().xor(rhs(), lhs());
       sp--;
    }

    public void stackGettype() {
        stack[sp - 1].set(new StringHeap(stack[sp - 1].typeName()));
    }

    public void cleanupStack() {
        for (int i = stack.length - 1; i >= sp; i--)
            stack[i].reset();
    }

    public void stackAload() {
        stack[sp - 2].testType(ValueType.MAP);
        Address val = stack[sp - 2].mapValue().get(stack[sp - 1]);
        stack[sp - 2].set(val);
        sp--;
    }

    public void stackAstore() {
        stack[sp - 3].testType(ValueType.MAP);
        stack[sp - 3].mapValue().put(stack[sp - 2], stack[sp - 1]);
        sp -= 3;
    }
    
    public void stackLDC(int constantIndex) {
        constant_pool().at(constantIndex).writeToAddress(top());
    }
}
