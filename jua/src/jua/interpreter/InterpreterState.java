package jua.interpreter;

import jua.interpreter.instruction.Instruction;
import jua.runtime.ValueType;
import jua.runtime.code.CodeSegment;
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

    InterpreterState(CodeSegment cs, InterpreterThread thread) {
        this.code = cs.code();
        this.stack = AddressUtils.allocateMemory(cs.maxStack(), 0);
        this.locals = AddressUtils.allocateMemory(cs.maxLocals(), 0);
        this.constantPool = cs.constantPool();
        this.thread = thread;
    }

    public Instruction currentInstruction() {
        return code[cp];
    }

    public void runDiscretely() {
        currentInstruction().run(this);
    }

    public InterpreterThread thread() {
        return thread;
    }

    public Instruction[] code() {
        return code;
    }

    public int cp() {
        return cp;
    }

    public void set_cp(int cp) {
        this.cp = cp;
    }

    public int sp() {
        return sp;
    }

    public void set_sp(int sp) {
        this.sp = sp;
    }

    public int cp_advance() {
        return cpAdvance;
    }

    public void set_cp_advance(int cpAdvance) {
        this.cpAdvance = cpAdvance;
    }

    public void next() {
        cp++;
    }

    public void offset(int offset) {
        cp += offset;
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

    public void getconst(int id) {
        top().set(thread.environment().getConstant(id));
        next();
    }

    @Deprecated
    public Address getConstantById(int id) {
        throw new UnsupportedOperationException();
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

    public Address top() {
        return stack[sp++];
    }


    public Address peekStack() {
        return first();
    }

    @Deprecated
    public void cleanupStack() {
        for (int i = stack.length - 1; i >= sp; i--)
            stack[i].reset();
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
                    thread().currentFrame().owningFunction().codeSegment().localTable().getLocalName(index) + "'.");
            return;
        }
        peekStack().set(locals[index]);
        sp++;
    }

    /* ОПЕРАЦИИ НА СТЕКЕ */

    public void push(short value) {
        pushStack(value);
        next();
    }

    public void pop() {
        sp--;
        next();
    }

    public void pop2() {
        sp -= 2;
        next();
    }

    public void dup() {
        Address peek = peekStack();
        top().set(peek);
        next();

    }

    public void dup2() {
        Address a = popStack();
        Address b = popStack();
        pushStack(b);
        pushStack(a);
        pushStack(b);
        pushStack(a);
        next();
    }

    public void dup1_x1() {
        stack[sp].set(first());
        first().set(second());
        second().set(stack[sp]);
        sp++;
        next();
    }

    private Address first() {
        return stack[sp - 1];
    }

    public void dup1_x2() {
        stack[sp].set(first());
        first().set(second());
        second().set(third());
        third().set(stack[sp]);
        sp++;
        next();
    }

    public void dup2_x1() {
        stack[sp + 1].set(first());
        stack[sp].set(second());
        second().set(third());
        third().set(stack[sp + 1]);
        stack[sp - 4].set(stack[sp]);
        sp += 2;
        next();
    }

    public void dup2_x2() {
        stack[sp + 1].set(first());
        stack[sp].set(second());
        first().set(third());
        second().set(stack[sp - 4]);
        stack[sp - 4].set(stack[sp + 1]);
        stack[sp - 5].set(stack[sp]);
        sp += 2;
        next();
    }

    public void stackAdd() {
        if (second().add(first(), second())) {
            sp--;
            next();
        }
    }

    public void stackCmp() {
        // todo: implement comparison of two last stack elements and pushing result to back
    }

    public void stackAnd() {
        if (second().and(first(), second())) {
            sp--;
            next();
        }
    }

    public void constFalse() {
        top().set(false);
        next();
    }

    public void constNull() {
        top().setNull();
        next();
    }

    public void constTrue() {
        top().set(true);
        next();
    }

    public void stackInc() {
        if (peekStack().inc(peekStack())) {
            next();
        }
    }

    public void stackDec() {
        if (peekStack().dec(peekStack())) {
            next();
        }
    }

    public void stackDiv() {
        if (second().div(first(), second())) {
            sp--;
            next();
        }
    }

    public void ifeq(int offset) {
        if (stackCmpeq()) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifne(int offset) {
        if (stackCmpne()) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifgt(int offset) {
        if (stackCmpgt()) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifge(int offset) {
        if (stackCmpge()) {
            offset(offset);
        } else {
            next();
        }
    }

    public void iflt(int offset) {
        if (stackCmplt()) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifle(int offset) {
        if (stackCmple()) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifconsteq(short value, int offset) {
        if (popStack().quickConstCompare(value, 1) == 0) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifconstne(short value, int offset) {
        if (popStack().quickConstCompare(value, 0) != 0) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifconstgt(short value, int offset) {
        if (popStack().quickConstCompare(value, 0) > 0) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifconstge(short value, int offset) {
        if (popStack().quickConstCompare(value, -1) >= 0) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifconstlt(short value, int offset) {
        if (popStack().quickConstCompare(value, 0) < 0) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifconstle(short value, int offset) {
        if (popStack().quickConstCompare(value, 1) <= 0) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifnull(int offset) {
        if (popStack().isNull()) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifnonnull(int offset) {
        if (!popStack().isNull()) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifz(int offset) {
        if (!popBoolean()) {
            offset(offset);
        } else {
            next();
        }
    }

    public void ifnz(int offset) {
        if (popBoolean()) {
            offset(offset);
        } else {
            next();
        }
    }

    final Address tmp = new Address();

    public Address getTemporalAddress() {
        return thread.getTempAddress();
    }

    private boolean popBoolean() {
        return popStack().booleanVal();
    }

    public boolean stackCmpeq() {
        int cmp = second().quickCompare(first(), 1);
        sp -= 2;
        return cmp == 0;
    }

    public boolean stackCmpne() {
        return !stackCmpeq();
    }

    public boolean stackCmpge() {
        int cmp = second().quickCompare(first(), -1);
        sp -= 2;
        return cmp >= 0;
    }

    public boolean stackCmpgt() {
        int cmp = second().quickCompare(first(), -1);
        sp -= 2;
        return cmp > 0;
    }

    public boolean stackCmple() {
        int cmp = second().quickCompare(first(), 1);
        sp -= 2;
        return cmp <= 0;
    }

    public boolean stackCmplt() {
        int cmp = second().quickCompare(first(), 1);
        sp -= 2;
        return cmp < 0;
    }

    public void stackLength() {
        if (first().length(first())) {
            next();
        }
    }

    public void stackMul() {
        if (second().mul(first(), second())) {
            sp--;
            next();
        }
    }

    public void stackNeg() {
        if (peekStack().neg(peekStack())) {
            next();
        }
    }

    public void stackNewArray() {
        top().set(new MapHeap());
        next();
    }

    public void stackNot() {
        if (peekStack().not(peekStack())) {
            next();
        }
    }

    public void stackNanosTime() {
        pushStack(System.nanoTime());
        next();
    }

    public void stackOr() {
        if (second().or(first(), second())) {
            sp--;
            next();
        }
    }

    public void stackPos() {
        peekStack().pos(peekStack());
        next();
    }

    public void stackRem() {
        if (second().rem(first(), second())) {
            sp--;
            next();
        }
    }

    public void stackShl() {
        if (second().shl(first(), second())) {
            sp--;
            next();
        }
    }

    public void stackShr() {
        if (second().shr(first(), second())) {
            sp--;
            next();
        }
    }

    public void stackSub() {
        if (second().sub(first(), second())) {
            sp--;
            next();
        }
    }

    public void stackXor() {
        if (second().xor(first(), second())) {
            sp--;
            next();
        }
    }

    public void stackGettype() {
        first().set(new StringHeap(first().getTypeName()));
        next();
    }

    public void stackAload() {
        if (second().load(first(), second())) {
            sp--;
            next();
        }
    }

    public void stackAstore() {
        if (third().store(second(), first())) {
            sp -= 3;
            next();
        }
    }

    private Address second() {
        return stack[sp - 2];
    }

    private Address third() {
        return stack[sp - 3];
    }

    public void stackLDC(int constantIndex) {
        constant_pool().at(constantIndex, top());
        next();
    }

    /* ОПЕРАЦИИ С ПЕРЕМЕННЫМИ */

    public void stack_quick_vdec(int id) {
        locals[id].dec(locals[id]);
        next();
    }

    public void stackVDec(int id) {
        if (testLocal(id)) {
            locals[id].dec(locals[id]);
            next();
        }
    }

    public void stack_quick_vinc(int id) {
        locals[id].inc(locals[id]);
        next();
    }

    public void stackVInc(int id) {
        if (testLocal(id)) {
            locals[id].inc(locals[id]);
            next();
        }
    }

    public void stack_quick_vload(int id) {
        pushStack(locals[id]);
        next();
    }

    public void stackVLoad(int id) {
        if (testLocal(id)) {
            pushStack(locals[id]);
            next();
        }
    }

    public void stackVStore(int id) {
        locals[id].set(popStack());
        next();
    }

    private boolean testLocal(int id) {
        if (locals[id].getType() == ValueType.UNDEFINED) {
            thread.error("Access to undefined variable: " +
                    thread.currentFrame().owningFunction().codeSegment().localTable().getLocalName(id));
            return false;
        }
        return true;
    }
}
