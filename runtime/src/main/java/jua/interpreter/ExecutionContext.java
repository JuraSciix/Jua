package jua.interpreter;

import jua.interpreter.address.Address;
import jua.runtime.Types;
import jua.runtime.code.ConstantPool;
import jua.runtime.heap.ListHeap;
import jua.runtime.heap.MapHeap;

public class ExecutionContext {

    private final InterpreterThread thread;

    private final InterpreterState state;

    private final ConstantPool constantPool;

    public ExecutionContext(InterpreterThread thread, InterpreterState state) {
        this.thread = thread;
        this.state = state;

        constantPool = thread
                .currentFrame()
                .owner()
                .userCode()
                .constantPool();
    }

    public InterpreterThread getThread() {
        return thread;
    }

    public InterpreterState getState() {
        return state;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public int getNextCp() {
        return state.getCp();
    }

    public void setNextCp(int nextCp) {
        getState().setCp(nextCp);
    }

    /*
     * =======================================================
     * ===============> РЕАЛИЗАЦИИ ИНСТРУКЦИЙ <===============
     * =======================================================
     *
     */

    public void doConstInt(long value) {
        getState().getStackAddress(0).set(value);
        getState().addTos(1);
    }

    public void doConstFalse() {
        getState().getStackAddress(0).set(false);
        getState().addTos(1);
    }

    public void doConstTrue() {
        getState().getStackAddress(0).set(true);
        getState().addTos(1);
    }

    public void doConstNull() {
        getState().getStackAddress(0).setNull();
        getState().addTos(1);
    }

    public void doPush(int cpi) { // Constant Pool Index
        getState().storeStackFrom(getConstantPool().getAddressEntry(cpi));
        getState().addTos(1);
    }

    public void doDup() {
        Address a1 = getState().getStackAddress(-1);
        Address a2 = getState().getStackAddress(0);
        a2.set(a1);
        getState().addTos(1);
    }

    public void doDup2() {
        Address a1 = getState().getStackAddress(-2);
        Address a2 = getState().getStackAddress(-1);
        Address a3 = getState().getStackAddress(0);
        Address a4 = getState().getStackAddress(1);
        a3.set(a1);
        a4.set(a2);
        getState().addTos(2);
    }

    public void doDupX1() {
        // Нужно переместить 2 элемента на 1 позицию вправо
        // Затем последний элемент скопировать в элемент на 2 позиции левее.
        Address a1 = getState().getStackAddress(-2);
        Address a2 = getState().getStackAddress(-1);
        Address a3 = getState().getStackAddress(0);
        a3.set(a2);
        a2.set(a1);
        a1.set(a3);
        getState().addTos(1);
    }

    public void doDupX2() {
        // Нужно переместить 3 элемента на 2 позиции вправо
        // Затем последний элемент скопировать в элемент на 3 позиции левее.
        Address a1 = getState().getStackAddress(-2);
        Address a2 = getState().getStackAddress(-1);
        Address a3 = getState().getStackAddress(0);
        Address a4 = getState().getStackAddress(1);
        a4.set(a3);
        a3.set(a2);
        a2.set(a1);
        a1.set(a4);
        getState().addTos(1);
    }

    public void doDup2x1() {
        // Нужно переместить 3 элемента на 2 позиции вправо
        // Затем 2 последних элементах скопировать в элементы на 3 позиции левее.
        Address a1 = getState().getStackAddress(0);
        Address a2 = getState().getStackAddress(1);
        Address a3 = getState().getStackAddress(2);
        Address a4 = getState().getStackAddress(3);
        Address a5 = getState().getStackAddress(4);
        a5.set(a4);
        a4.set(a3);
        a3.set(a2);
        a2.set(a4);
        a1.set(a3);
        getState().addTos(2);
    }

    public void doDup2x2() {
        // Нужно переместить 4 элемента на 2 позиции вправо
        // Затем 2 последних элементах скопировать в элементы на 4 позиции левее.
        Address a1 = getState().getStackAddress(0);
        Address a2 = getState().getStackAddress(1);
        Address a3 = getState().getStackAddress(2);
        Address a4 = getState().getStackAddress(3);
        Address a5 = getState().getStackAddress(4);
        Address a6 = getState().getStackAddress(5);
        a6.set(a5);
        a5.set(a4);
        a4.set(a3);
        a3.set(a2);
        a2.set(a6);
        a1.set(a5);
        getState().addTos(2);
    }

    public void doPop() {
        // Ячейка стека будет очищена при попытке выделить много памяти
        // или возврате из метода.
        getState().addTos(-1);
    }

    public void doPop2() {
        getState().addTos(-2);
    }

    public void doAdd() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.add(rhs, lhs);
        getState().addTos(-1);
    }

    public void doSub() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.sub(rhs, lhs);
        getState().addTos(-1);
    }

    public void doDiv() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.div(rhs, lhs);
        getState().addTos(-1);
    }

    public void doMul() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.mul(rhs, lhs);
        getState().addTos(-1);
    }

    public void doRem() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.rem(rhs, lhs);
        getState().addTos(-1);
    }

    public void doAnd() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.and(rhs, lhs);
        getState().addTos(-1);
    }

    public void doOr() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.or(rhs, lhs);
        getState().addTos(-1);
    }

    public void doXor() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.xor(rhs, lhs);
        getState().addTos(-1);
    }

    public void doShl() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.shl(rhs, lhs);
        getState().addTos(-1);
    }

    public void doShr() {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        lhs.shr(rhs, lhs);
        getState().addTos(-1);
    }

    public void doPos() {
        Address value = getState().getStackAddress(-1);
        value.pos(value);
    }

    public void doNeg() {
        Address value = getState().getStackAddress(-1);
        value.neg(value);
    }

    public void doNot() {
        Address value = getState().getStackAddress(-1);
        value.not(value);
    }

    public void doLength() {
        Address value = getState().getStackAddress(-1);
        value.length(value);
    }

    public void doLoad(int i) {
        getState().storeStackFrom(getState().getSlot(i));
        getState().addTos(1);
    }

    public void doStore(int i) {
        getState().storeSlotFrom(i, getState().getStackAddress(-1));
        getState().addTos(-1);
    }

    public void doInc(int i) {
        getState().getSlot(i).inc();
    }

    public void doDec(int i) {
        getState().getSlot(i).dec();
    }

    public void doArrayLoad() {
        Address arr = getState().getStackAddress(-2);
        Address key = getState().getStackAddress(-1);
        arr.load(key, arr);
    }

    public void doArrayStore() {
        Address arr = getState().getStackAddress(-3);
        Address key = getState().getStackAddress(-2);
        Address val = getState().getStackAddress(-1);
        arr.store(key, val);
        getState().addTos(-3);
    }

    public void doArrayInc() {
        Address arr = getState().getStackAddress(-2);
        Address key = getState().getStackAddress(-1);
        arr.arrayInc(key, arr);
        getState().addTos(-1);
    }

    public void doArrayDec() {
        Address arr = getState().getStackAddress(-2);
        Address key = getState().getStackAddress(-1);
        arr.arrayDec(key, arr);
        getState().addTos(-1);
    }

    public void doNewList() {
        Address value = getState().getStackAddress(0);
        long a;
        if (!value.hasType(Types.T_INT) ||
                (a = value.getLong()) < 0 ||
                Integer.MAX_VALUE < a) {
            getThread().error("List size must be an unsigned 32-bit integer");
            return;
        }
        value.set(new ListHeap((int) a));
    }

    public void doNewMap() {
        getState().getStackAddress(0).set(new MapHeap());
        getState().addTos(1);
    }


    public void doJumpIfEq(int nextCp) {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        getState().addTos(-2);
        if (lhs.fastCompareWith(rhs, 1) == 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfntEq(int nextCp) {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        getState().addTos(-2);
        if (lhs.fastCompareWith(rhs, 0) != 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfGt(int nextCp) {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        getState().addTos(-2);
        if (lhs.fastCompareWith(rhs, -1) > 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfGe(int nextCp) {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        getState().addTos(-2);
        if (lhs.fastCompareWith(rhs, -1) >= 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfLt(int nextCp) {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        getState().addTos(-2);
        if (lhs.fastCompareWith(rhs, 1) < 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfLe(int nextCp) {
        Address lhs = getState().getStackAddress(-2);
        Address rhs = getState().getStackAddress(-1);
        getState().addTos(-2);
        if (lhs.fastCompareWith(rhs, 1) <= 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfNull(int nextCp) {
        Address value = getState().getStackAddress(-1);
        getState().addTos(-1);
        if (value.isNull()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfTrue(int nextCp) {
        Address value = getState().getStackAddress(-1);
        getState().addTos(-1);
        if (value.booleanVal()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfFalse(int nextCp) {
        Address value = getState().getStackAddress(-1);
        getState().addTos(-1);
        if (!value.booleanVal()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfntNull(int nextCp) {
        Address value = getState().getStackAddress(-1);
        getState().addTos(-1);
        if (!value.isNull()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfPresent(int nextCp) {
        Address arr = getState().getStackAddress(-2);
        Address key = getState().getStackAddress(-1);
        getState().addTos(-2);
        if (arr.contains(key) == Address.RESULT_TRUE) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfAbsent(int nextCp) {
        Address arr = getState().getStackAddress(-2);
        Address key = getState().getStackAddress(-1);
        getState().addTos(-2);
        if (arr.contains(key) == Address.RESULT_FALSE) {
            setNextCp(nextCp);
        }
    }

    public void doLinearSwitch(int[] labels, int[] cps, int defaultCp) {
        Address selector = getState().getStackAddress(-1);
        getState().addTos(-1);

        // Не скалярные значения семантически запрещены
        if (!selector.isScalar()) {
            setNextCp(defaultCp);
            return;
        }

        for (int i = 0; i < labels.length; i++) {
            Address k = getConstantPool().getAddressEntry(labels[i]);
            if (selector.fastCompareWith(k, 1) == 0) {
                setNextCp(cps[i]);
                return;
            }
        }
        setNextCp(defaultCp); /* default ip */
    }

    public void doBinarySwitch(int[] labels, int[] cps, int defaultCp) {
        Address selector = getState().getStackAddress(-1);
        getState().addTos(-1);

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
        int tos = getState().getTos();
        getThread().prepareCall(calleeId, argCount,
                getState().getStack().subMemory(tos - argCount, Math.max(argCount, 1)));
        getState().addTos(-argCount + 1);
    }

    public void doGetConst(int constantId) {
        getState().storeStackFrom(getThread().getEnvironment().getConstant(constantId));
    }

    public void doReturn() {
        Address returnee = getState().getStackAddress(-1);
        getState().addTos(-1);
        getThread().doReturn(returnee);
    }

    public void doLeave() {
        getThread().leave();
    }
}
