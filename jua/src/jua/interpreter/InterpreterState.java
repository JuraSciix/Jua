package jua.interpreter;

import jua.interpreter.instruction.Instruction;
import jua.runtime.ValueType;
import jua.runtime.code.CodeData;
import jua.runtime.code.ConstantPool;
import jua.runtime.heap.ListHeap;
import jua.runtime.heap.MapHeap;
import jua.runtime.heap.Operand;

public final class InterpreterState {



    /** Указатель на текущую инструкцию. */
    private int cp;
    /** Указатель на вершину стека. */
    private int sp;

    /** Стек. */
    private final Address[] stack;
    /** Регистры. */
    private final Address[] slots;

    // todo: Удалить нижеперечисленные поля

    @Deprecated private final Instruction[] code;
    @Deprecated private final CodeData cs;
    @Deprecated private int cpAdvance;
    @Deprecated private final InterpreterThread thread;

    InterpreterState(CodeData cs, InterpreterThread thread) {
        this.code = cs.code;
        this.stack = AddressUtils.allocateMemory(cs.stackSize, 0);
        this.slots = AddressUtils.allocateMemory(cs.registers, 0);
        this.cs = cs;
        this.thread = thread;
    }

    public Instruction currentInstruction() {
        return code[cp];
    }

    public void executeTick() {
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

    public ConstantPool constant_pool() {
        return cs.constantPool;
    }

    public void advance() {
        cp += cpAdvance;
        cpAdvance = 0;
    }

    public void getconst(int id) {
        top().set(thread.environment().getConstant(id));
        next();
    }

    public void pushStack(long value) {
        top().set(value);
    }

    public void pushStack(Address address) {
        stack[sp++].set(address);
    }

    private Address popStack() {
        return stack[--sp];
    }

    private Address top() {
        return stack[sp++];
    }

    private Address first() { return stack[sp - 1]; }

    private Address second() { return stack[sp - 2]; }

    private Address third() { return stack[sp - 3]; }

    private Address peekStack() {
        return first();
    }

    public void cleanupStack() {
        for (int i = stack.length - 1; i > sp; i--)
            stack[i].reset();
    }

    @Deprecated
    public void store(int index, Operand value) {
        value.writeToAddress(slots[index]);
    }

    public void store(int index, Address value) {
        slots[index].set(value);
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

    public void constInt(long value) {
        top().set(value);
        next();
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

    public void stack_ainc() {
        Address key = popStack();
        Address val = popStack();

        if (val.arrayInc(key, top())) {
            next();
        }
    }

    public void stack_adec() {
        Address key = popStack();
        Address val = popStack();

        if (val.arrayDec(key, top())) {
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

    private boolean popBoolean() {
        return popStack().booleanVal();
    }

    public boolean stackCmpeq() {
        int cmp = second().weakCompare(first(), 1);
        sp -= 2;
        return cmp == 0;
    }

    public boolean stackCmpne() {
        return !stackCmpeq();
    }

    public boolean stackCmpge() {
        int cmp = second().weakCompare(first(), -1);
        sp -= 2;
        return cmp >= 0;
    }

    public boolean stackCmpgt() {
        int cmp = second().weakCompare(first(), -1);
        sp -= 2;
        return cmp > 0;
    }

    public boolean stackCmple() {
        int cmp = second().weakCompare(first(), 1);
        sp -= 2;
        return cmp <= 0;
    }

    public boolean stackCmplt() {
        int cmp = second().weakCompare(first(), 1);
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

    public void stack_newmap() {
        top().set(new MapHeap());
        next();
    }

    public void stack_newlist() {
        Address sizeAddress = popStack();
        if (!sizeAddress.testType(ValueType.LONG)) {
            return;
        }
        long size = sizeAddress.getLong();
        if (size < 0 || size > Integer.MAX_VALUE) {
            thread.error("illegal list size: %d", size);
            return;
        }
        top().set(new ListHeap((int) size));
        next();
    }

    public void stackNot() {
        if (peekStack().not(peekStack())) {
            next();
        }
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

    public void stackLDC(int constantIndex) {
        constant_pool().load(constantIndex, top());
        next();
    }

    public void stackVDec(int id) {
        if (slots[id].dec()) {
            next();
        }
    }

    public void stackVInc(int id) {
        if (slots[id].inc()) {
            next();
        }
    }

    public void stackVLoad(int id) {
        pushStack(slots[id]);
        next();
    }

    public void stackVStore(int id) {
        slots[id].set(popStack());
        next();
    }

    public void impl_return() {
        thread().doReturn(popStack());
    }

    public void impl_binaryswitch(int[] literals, int[] destIps, int offset) {
        // Новый, двоичный поиск
        ConstantPool cp = constant_pool();

        int l = 0;
        int h = literals.length - 1;

        Address sel = popStack();           /* selector */
        Address tmp = new Address(); /* buffer   */

        // Не скалярные значения семантически запрещены
        if (sel.isScalar()) {
            while (l <= h) {
                int i = (l + h) >> 1;
                cp.load(literals[i], tmp);

                int d = sel.compareTo(tmp);

                if (d > 0) {
                    l = i + 1;
                } else if (d < 0) {
                    h = i - 1;
                } else {
                    /* assert d != 2; sel == tmp */
                    offset(destIps[i]);
                    return;
                }
            }
        }

        offset(offset); /* default offset */
    }

    public void impl_linearswitch(int[] literals, int[] destIps, int offset) {
        Address selector = popStack();

        Address tmp = new Address();
        for (int i = 0; i < literals.length; i++) {
            constant_pool().load(literals[i], tmp);
            if (selector.compareTo(tmp) == 0) {
                offset(destIps[i]);
                return;
            }
        }
        offset(offset); /* default ip */
    }

    public void impl_call(int index, int nargs) {
        Address[] args = new Address[nargs];

        int i = nargs;
        while (--i >= 0) {
            args[i] = popStack();
        }

        Address returnAddress = top();
        thread().prepareCall(index, args, nargs, returnAddress, false);
        cleanupStack();
        set_cp_advance(1);
    }
}
