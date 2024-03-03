package jua.runtime.interpreter;

import jua.runtime.Function;
import jua.runtime.Types;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.ResolvableCallee;
import jua.runtime.heap.ListHeap;
import jua.runtime.interpreter.memory.Address;

import static jua.runtime.Operations.isResultFalse;
import static jua.runtime.Operations.isResultTrue;

public final class ExecutionContext {

    private final InterpreterThread thread;

    private InterpreterState state;

    private ConstantPool constantPool;

    public ExecutionContext(InterpreterThread thread) {
        this.thread = thread;
    }

    public InterpreterThread getThread() {
        return thread;
    }

    public ThreadStack stack() {
        return thread.stack();
    }

    public void setState(InterpreterState state) {
        this.state = state;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public void setConstantPool(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    public int getNextCp() {
        return state.getCp();
    }

    public void setNextCp(int nextCp) {
        state.setCp(nextCp);
    }

    /*
     * =======================================================
     * ===============> РЕАЛИЗАЦИИ ИНСТРУКЦИЙ <===============
     * =======================================================
     *
     */

    public void doConstInt(long value) {
        stack().push().set(value);
    }

    public void doConstFalse() {
        stack().push().set(false);
    }

    public void doConstTrue() {
        stack().push().set(true);
    }

    public void doConstNull() {
        stack().push().setNull();
    }

    public void doPush(int cpi) { // Constant Pool Index
        stack().push().set(getConstantPool().getAddressEntry(cpi));
    }

    public void doDup() {
        Address a1 = stack().getStackAddress(-1);
        Address a2 = stack().getStackAddress(0);
        a2.set(a1);
        a2.set(a1);
        stack().addTos(1);
    }

    public void doDup2() {
        Address a1 = stack().getStackAddress(-2);
        Address a2 = stack().getStackAddress(-1);
        Address a3 = stack().getStackAddress(0);
        Address a4 = stack().getStackAddress(1);
        a3.set(a1);
        a4.set(a2);
        stack().addTos(2);
    }

    public void doDupX1() {
        // Нужно переместить 2 элемента на 1 позицию вправо
        // Затем последний элемент скопировать в элемент на 2 позиции левее.
        Address a1 = stack().getStackAddress(-2);
        Address a2 = stack().getStackAddress(-1);
        Address a3 = stack().getStackAddress(0);
        a3.set(a2);
        a2.set(a1);
        a1.set(a3);
        stack().addTos(1);
    }

    public void doDupX2() {
        // Нужно переместить 3 элемента на 2 позиции вправо
        // Затем последний элемент скопировать в элемент на 3 позиции левее.
        Address a1 = stack().getStackAddress(-3);
        Address a2 = stack().getStackAddress(-2);
        Address a3 = stack().getStackAddress(-1);
        Address a4 = stack().getStackAddress(0);
        a4.set(a3);
        a3.set(a2);
        a2.set(a1);
        a1.set(a4);
        stack().addTos(1);
    }

    public void doDup2x1() {
        // Нужно переместить 3 элемента на 2 позиции вправо
        // Затем 2 последних элементах скопировать в элементы на 3 позиции левее.

        // -3 -2 -1  0  1
        //  H  A  B  _  _
        //  _  _  H  A  B
        //  A  B  H  A  B
        Address m3 = stack().getStackAddress(-3);
        Address m2 = stack().getStackAddress(-2);
        Address m1 = stack().getStackAddress(-1);
        Address p0 = stack().getStackAddress(0);
        Address p1 = stack().getStackAddress(1);
        p1.set(m1);
        p0.set(m2);
        m1.set(m3);
        m2.set(p1);
        m3.set(p0);
        stack().addTos(2);
    }

    public void doDup2x2() {
        // Нужно переместить 4 элемента на 2 позиции вправо
        // Затем 2 последних элементах скопировать в элементы на 4 позиции левее.

        // -4 -3 -2 -1  0  1
        //  G  H  A  B  _  _
        //  G  H  G  H  A  B
        //  A  B  G  H  A  B
        Address m4 = stack().getStackAddress(-4);
        Address m3 = stack().getStackAddress(-3);
        Address m2 = stack().getStackAddress(-2);
        Address m1 = stack().getStackAddress(-1);
        Address p0 = stack().getStackAddress(0);
        Address p1 = stack().getStackAddress(1);
        p1.set(m1);
        p0.set(m2);
        m1.set(m3);
        m2.set(m4);
        m3.set(p1);
        m4.set(p0);
        stack().addTos(2);
    }

    public void doPop() {
        // Ячейка стека будет очищена при попытке выделить много памяти
        // или возврате из метода.
        stack().addTos(-1);
    }

    public void doPop2() {
        stack().addTos(-2);
    }

    public void doAdd() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.add(rhs, lhs);
        stack().addTos(-1);
    }

    public void doSub() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.sub(rhs, lhs);
        stack().addTos(-1);
    }

    public void doDiv() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.div(rhs, lhs);
        stack().addTos(-1);
    }

    public void doMul() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.mul(rhs, lhs);
        stack().addTos(-1);
    }

    public void doRem() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.rem(rhs, lhs);
        stack().addTos(-1);
    }

    public void doAnd() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.and(rhs, lhs);
        stack().addTos(-1);
    }

    public void doOr() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.or(rhs, lhs);
        stack().addTos(-1);
    }

    public void doXor() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.xor(rhs, lhs);
        stack().addTos(-1);
    }

    public void doShl() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.shl(rhs, lhs);
        stack().addTos(-1);
    }

    public void doShr() {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        lhs.shr(rhs, lhs);
        stack().addTos(-1);
    }

    public void doPos() {
        Address value = stack().getStackAddress(-1);
        value.pos(value);
    }

    public void doNeg() {
        Address value = stack().getStackAddress(-1);
        value.neg(value);
    }

    public void doNot() {
        Address value = stack().getStackAddress(-1);
        value.not(value);
    }

    public void doLength() {
        Address value = stack().getStackAddress(-1);
        value.length(value);
    }

    public void doLoad(int i) {
        stack().push(state.getSlot(i));
    }

    public void doStore(int i) {
        state.storeSlotFrom(i, stack().getStackAddress(-1));
        stack().addTos(-1);
    }

    public void doInc(int i) {
        state.getSlot(i).inc();
    }

    public void doDec(int i) {
        state.getSlot(i).dec();
    }

    public void doArrayLoad() {
        Address arr = stack().getStackAddress(-2);
        Address key = stack().getStackAddress(-1);
        arr.load(key, arr);
        stack().addTos(-1);
    }

    public void doArrayStore() {
        Address arr = stack().getStackAddress(-3);
        Address key = stack().getStackAddress(-2);
        Address val = stack().getStackAddress(-1);
        arr.store(key, val);
        stack().addTos(-3);
    }

    public void doArrayInc() {
        Address arr = stack().getStackAddress(-2);
        Address key = stack().getStackAddress(-1);
        arr.arrayInc(key, arr);
        stack().addTos(-1);
    }

    public void doArrayDec() {
        Address arr = stack().getStackAddress(-2);
        Address key = stack().getStackAddress(-1);
        arr.arrayDec(key, arr);
        stack().addTos(-1);
    }

    public void doNewList() {
        Address value = stack().getStackAddress(-1);
        long a;
        if (!value.hasType(Types.T_INT) ||
                (a = value.getLong()) < 0 ||
                Integer.MAX_VALUE < a) {
            getThread().error("List size must be an unsigned 32-bit integer");
            return;
        }
        value.set(new ListHeap((int) a));
    }

    public void doJumpIfEq(int nextCp) {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        stack().addTos(-2);
        if (lhs.fastCompareWith(rhs, 1) == 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfntEq(int nextCp) {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        stack().addTos(-2);
        if (lhs.fastCompareWith(rhs, 0) != 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfGt(int nextCp) {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        stack().addTos(-2);
        if (lhs.fastCompareWith(rhs, -1) > 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfGe(int nextCp) {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        stack().addTos(-2);
        if (lhs.fastCompareWith(rhs, -1) >= 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfLt(int nextCp) {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        stack().addTos(-2);
        if (lhs.fastCompareWith(rhs, 1) < 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfLe(int nextCp) {
        Address lhs = stack().getStackAddress(-2);
        Address rhs = stack().getStackAddress(-1);
        stack().addTos(-2);
        if (lhs.fastCompareWith(rhs, 1) <= 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfNull(int nextCp) {
        Address value = stack().getStackAddress(-1);
        stack().addTos(-1);
        if (value.isNull()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfNonZero(int nextCp) {
        Address value = stack().getStackAddress(-1);
        stack().addTos(-1);
        if (value.booleanVal()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfZero(int nextCp) {
        Address value = stack().getStackAddress(-1);
        stack().addTos(-1);
        if (!value.booleanVal()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfntNull(int nextCp) {
        Address value = stack().getStackAddress(-1);
        stack().addTos(-1);
        if (!value.isNull()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfPresent(int nextCp) {
        Address arr = stack().getStackAddress(-2);
        Address key = stack().getStackAddress(-1);
        stack().addTos(-2);
        int responseCode = arr.contains(key);
        if (isResultTrue(responseCode)) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfAbsent(int nextCp) {
        Address arr = stack().getStackAddress(-2);
        Address key = stack().getStackAddress(-1);
        stack().addTos(-2);
        int responseCode = arr.contains(key);
        if (isResultFalse(responseCode)) {
            setNextCp(nextCp);
        }
    }

    public void doLinearSwitch(int[] labels, int[] cps, int defaultCp) {
        Address selector = stack().getStackAddress(-1);
        stack().addTos(-1);

        // Не скалярные значения семантически запрещены
        if (!selector.isScalar()) {
            setNextCp(defaultCp);
            return;
        }

        int selectorHash = selector.hashCode();

        for (int i = 0; i < labels.length; i++) {
            Address k = getConstantPool().getAddressEntry(labels[i]);
            int kHash = k.hashCode();
            if (selectorHash == kHash && selector.fastCompareWith(k, 1) == 0) {
                setNextCp(cps[i]);
                return;
            }
        }
        setNextCp(defaultCp); /* default ip */
    }

    public void doBinarySwitch(int[] labels, int[] cps, int defaultCp) {
        Address selector = stack().getStackAddress(-1);
        stack().addTos(-1);

        // Не скалярные значения семантически запрещены
        if (!selector.isScalar()) {
            setNextCp(defaultCp);
            return;
        }

        int l = 0;
        int h = labels.length - 1;

        while (l <= h) {
            int x = (l + h) >> 1;
            Address k = getConstantPool().getAddressEntry(labels[x]);
            int d = selector.compareTo(k);

            if (d > 0) {
                l = x + 1;
            } else if (d < 0) {
                h = x - 1;
            } else {
                // Не помню, почему d не должно равняться 2, но удалять не буду — вдруг что-то важное.
                /* assert d != 2; */

                // Если selector != k, значит один из операндов это NaN и цикл все равно завершен.
                if (selector.fastCompareWith(k, 1) == 0) {
                    setNextCp(cps[x]);
                }
                return;
            }
        }

        setNextCp(defaultCp); /* default offset */
    }

    public void doCall(int calleeId, int argCount) {
        ResolvableCallee callee = getConstantPool().getCallee(calleeId);
        Function fn;
        if (callee.isResolved()) {
            fn = callee.getResolved();
        } else {
            String name = getConstantPool().getAddressEntry(callee.getUtf8()).stringVal().toString();
            fn = getThread().getEnvironment().lookupFunction(name);
            callee.setResolved(fn);
        }
        getThread().prepareCall(fn, argCount);
//        state.addTos(-argCount + 1);
    }

    public void doReturn() {
        getThread().doReturn();
    }

    public void doLeave() {
        getThread().leave();
    }
}
