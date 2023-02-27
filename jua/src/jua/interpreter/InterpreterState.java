package jua.interpreter;

import jua.interpreter.instruction.Instruction;
import jua.runtime.ValueType;
import jua.runtime.code.CodeData;
import jua.runtime.code.ConstantPool;
import jua.runtime.heap.ListHeap;
import jua.runtime.heap.MapHeap;

public final class InterpreterState {

    /** Указатель на текущую инструкцию. */
    private int cp = -1;
    /** Указатель на вершину стека. */
    private int sp;

    /** Стек. */
    private final Address[] stack;
    /** Регистры. */
    private final Address[] slots;

    InterpreterState(CodeData cs) {
        this.stack = AddressUtils.allocateMemory(cs.stackSize, 0);
        this.slots = AddressUtils.allocateMemory(cs.registers, 0);
    }

    public void executeTick(InterpreterThread thread, InterpreterFrame frame) {
        cp++;
        Instruction[] code = frame.owner.userCode().code;
        while (true) {
            if (!code[cp].run(this)) {
                return;
            }
        }
    }

    public InterpreterThread thread() {
        return InterpreterThread.currentThread();
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

    public void next() {
        cp++;
    }

    public void offset(int offset) {
        cp += offset;
    }

    public boolean _goto(int offset) {
        offset(offset);
        return true;
    }

    public ConstantPool constant_pool() {
        return thread().executingFrame.owner.userCode().constantPool;
    }

    public boolean getconst(int id) {
        top().set(thread().environment().getConstant(id));
        next();
        return true;
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

    public void store(int index, Address value) {
        slots[index].set(value);
    }

    /* ОПЕРАЦИИ НА СТЕКЕ */

    public boolean pop() {
        sp--;
        next();
        return true;
    }

    public boolean pop2() {
        sp -= 2;
        next();
        return true;
    }

    public boolean dup() {
        Address peek = peekStack();
        top().set(peek);
        next();
        return true;
    }

    public boolean dup2() {
        Address a = popStack();
        Address b = popStack();
        pushStack(b);
        pushStack(a);
        pushStack(b);
        pushStack(a);
        next();
        return true;
    }

    public boolean dup1_x1() {
        stack[sp].set(first());
        first().set(second());
        second().set(stack[sp]);
        sp++;
        next();
        return true;
    }

    public boolean dup1_x2() {
        stack[sp].set(first());
        first().set(second());
        second().set(third());
        third().set(stack[sp]);
        sp++;
        next();
        return true;
    }

    public boolean dup2_x1() {
        stack[sp + 1].set(first());
        stack[sp].set(second());
        second().set(third());
        third().set(stack[sp + 1]);
        stack[sp - 4].set(stack[sp]);
        sp += 2;
        next();
        return true;
    }

    public boolean dup2_x2() {
        stack[sp + 1].set(first());
        stack[sp].set(second());
        first().set(third());
        second().set(stack[sp - 4]);
        stack[sp - 4].set(stack[sp + 1]);
        stack[sp - 5].set(stack[sp]);
        sp += 2;
        next();
        return true;
    }

    public boolean stackAdd() {
        if (second().add(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean stackCmp() {
        // todo: implement comparison of two last stack elements and pushing result to back
        return true;
    }

    public boolean stackAnd() {
        if (second().and(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean constInt(long value) {
        top().set(value);
        next();
        return true;
    }

    public boolean constFalse() {
        top().set(false);
        next();
        return true;
    }

    public boolean constNull() {
        top().setNull();
        next();
        return true;
    }

    public boolean constTrue() {
        top().set(true);
        next();
        return true;
    }

    public boolean stack_ainc() {
        Address key = popStack();
        Address val = popStack();

        if (val.arrayInc(key, top())) {
            next();
            return true;
        }
        return false;
    }

    public boolean stack_adec() {
        Address key = popStack();
        Address val = popStack();

        if (val.arrayDec(key, top())) {
            next();
            return true;
        }
        return false;
    }

    public boolean stackDiv() {
        if (second().div(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean ifeq(int offset) {
        if (stackCmpeq()) {
            offset(offset);
        } else {
            next();
        }
        return true;
    }

    public boolean ifne(int offset) {
        if (stackCmpne()) {
            offset(offset);
        } else {
            next();
        }
        return true;
    }

    public boolean ifgt(int offset) {
        if (stackCmpgt()) {
            offset(offset);
        } else {
            next();
        }
        return true;
    }

    public boolean ifge(int offset) {
        if (stackCmpge()) {
            offset(offset);
        } else {
            next();
        }
        return true;
    }

    public boolean iflt(int offset) {
        if (stackCmplt()) {
            offset(offset);
        } else {
            next();
        }
        return true;
    }

    public boolean ifle(int offset) {
        if (stackCmple()) {
            offset(offset);
        } else {
            next();
        }
        return true;
    }

    public boolean ifnull(int offset) {
        if (popStack().isNull()) {
            offset(offset);
        } else {
            next();
        }
        return true;
    }

    public boolean ifnonnull(int offset) {
        if (!popStack().isNull()) {
            offset(offset);
        } else {
            next();
        }
        return false;
    }

    public boolean ifz(int offset) {
        if (!popBoolean()) {
            offset(offset);
        } else {
            next();
        }
        return true;
    }

    public boolean ifnz(int offset) {
        if (popBoolean()) {
            offset(offset);
        } else {
            next();
        }
        return true;
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

    public boolean stackLength() {
        if (first().length(first())) {
            next();
            return true;
        }
        return false;
    }

    public boolean stackMul() {
        if (second().mul(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean stackNeg() {
        if (peekStack().neg(peekStack())) {
            next();
            return true;
        }
        return false;
    }

    public boolean stack_newmap() {
        top().set(new MapHeap());
        next();
        return true;
    }

    public boolean stack_newlist() {
        Address sizeAddress = popStack();
        if (!sizeAddress.testType(ValueType.LONG)) {
            return false;
        }
        long size = sizeAddress.getLong();
        if (size < 0 || size > Integer.MAX_VALUE) {
            thread().error("illegal list size: %d", size);
            return false;
        }
        top().set(new ListHeap((int) size));
        next();
        return true;
    }

    public boolean stackNot() {
        if (peekStack().not(peekStack())) {
            next();
            return true;
        }
        return false;
    }

    public boolean stackOr() {
        if (second().or(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean stackPos() {
        peekStack().pos(peekStack());
        next();
        return true;
    }

    public boolean stackRem() {
        if (second().rem(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean stackShl() {
        if (second().shl(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean stackShr() {
        if (second().shr(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean stackSub() {
        if (second().sub(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean stackXor() {
        if (second().xor(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean stackAload() {
        if (second().load(first(), second())) {
            sp--;
            next();
            return true;
        }
        return false;
    }

    public boolean stackAstore() {
        if (third().store(second(), first())) {
            sp -= 3;
            next();
            return true;
        }
        return false;
    }

    public boolean stackPush(int constantIndex) {
        top().set(constant_pool().getAddress(constantIndex));
        next();
        return true;
    }

    public boolean stackVDec(int id) {
        if (slots[id].dec()) {
            next();
            return true;
        }
        return false;
    }

    public boolean stackVInc(int id) {
        if (slots[id].inc()) {
            next();
            return true;
        }
        return false;
    }

    public boolean stackVLoad(int id) {
        pushStack(slots[id]);
        next();
        return true;
    }

    public boolean stackVStore(int id) {
        slots[id].set(popStack());
        next();
        return true;
    }

    public boolean impl_return() {
        thread().doReturn(popStack());
        return false;
    }

    public boolean impl_leave() {
        thread().leave();
        return false;
    }

    public boolean impl_binaryswitch(int[] literals, int[] destIps, int offset) {
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
                tmp.set(cp.getAddress(literals[i]));

                int d = sel.compareTo(tmp);

                if (d > 0) {
                    l = i + 1;
                } else if (d < 0) {
                    h = i - 1;
                } else {
                    /* assert d != 2; sel == tmp */
                    offset(destIps[i]);
                    return true;
                }
            }
        }

        offset(offset); /* default offset */
        return true;
    }

    public boolean impl_linearswitch(int[] literals, int[] destIps, int offset) {
        Address selector = popStack();

        Address tmp = new Address();
        for (int i = 0; i < literals.length; i++) {
            tmp.set(constant_pool().getAddress(literals[i]));
            if (selector.compareTo(tmp) == 0) {
                offset(destIps[i]);
                return true;
            }
        }
        offset(offset); /* default ip */
        return true;
    }

    public boolean impl_call(int index, int nargs) {
        Address[] args = new Address[nargs];

        int i = nargs;
        while (--i >= 0) {
            args[i] = new Address();
            args[i].set(popStack());
        }

        Address returnAddress = top();
        // todo: Получать ссылку на функцию из пула констант
        thread().prepareCall(thread().environment().getFunction(index), args, nargs, returnAddress);
        cleanupStack();
        return false;
    }

    public boolean nop() {
        next();
        return true;
    }
}
