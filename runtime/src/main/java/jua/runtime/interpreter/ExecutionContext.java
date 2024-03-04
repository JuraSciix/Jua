package jua.runtime.interpreter;

import jua.runtime.Function;
import jua.runtime.Types;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.ResolvableCallee;
import jua.runtime.heap.ListHeap;

import static jua.runtime.Operations.isResultFalse;
import static jua.runtime.Operations.isResultTrue;

public final class ExecutionContext {

    private final InterpreterThread thread;

    private InterpreterFrame frame;

    private ConstantPool constantPool;

    public ExecutionContext(InterpreterThread thread) {
        this.thread = thread;
    }

    public InterpreterThread thread() {
        return thread;
    }

    public ThreadStack stack() {
        return thread().stack();
    }

    public ThreadMemory memory() {
        return thread().memory();
    }

    public void setFrame(InterpreterFrame frame) {
        this.frame = frame;
        constantPool = frame.getFunction().getCode().constantPool();
    }

    public ConstantPool constantPool() {
        return constantPool;
    }

    public int getNextCp() {
        return frame.getCP();
    }

    public void setNextCp(int nextCp) {
        frame.setCP(nextCp);
    }

    /*
     * =======================================================
     * ===============> РЕАЛИЗАЦИИ ИНСТРУКЦИЙ <===============
     * =======================================================
     *
     */

    public void doConstInt(long value) {
        stack().pushGet().set(value);
    }

    public void doConstFalse() {
        stack().pushGet().set(false);
    }

    public void doConstTrue() {
        stack().pushGet().set(true);
    }

    public void doConstNull() {
        stack().pushGet().setNull();
    }

    public void doPush(int cpi) { // Constant Pool Index
        stack().pushGet().set(constantPool().getAddressEntry(cpi));
    }

    public void doDup() {
        stack().dup();
    }

    public void doDup2() {
        stack().dup2();
    }

    public void doDupX1() {
        stack().dupX1();
    }

    public void doDupX2() {
        stack().dupX2();
    }

    public void doDup2x1() {
        stack().dup2X1();
    }

    public void doDup2x2() {
        stack().dup2X2();
    }

    public void doPop() {
        stack().pop();
    }

    public void doPop2() {
        stack().pop2();
    }

    public void doAdd() {
        Address rhs = stack().popGet();
        Address lhs = stack().popGet();
        lhs.add(rhs, lhs);
        stack().push(lhs);
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
        stack().push(memory().get(i));
    }

    public void doStore(int i) {
        memory().get(i).set(stack().getStackAddress(-1));
        stack().addTos(-1);
    }

    public void doInc(int i) {
        memory().get(i).inc();
    }

    public void doDec(int i) {
        memory().get(i).dec();
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
            thread().error("List size must be an unsigned 32-bit integer");
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

    public void doJumpIfntEq(int thenCp) {
        Address rhs = stack().popGet();
        Address lhs = stack().popGet();
        if (lhs.fastCompareWith(rhs, 1) != 0)
            setNextCp(thenCp);
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
        Address key = stack().popGet();
        Address arr = stack().popGet();
        int responseCode = arr.contains(key);
        if (isResultTrue(responseCode)) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfAbsent(int nextCp) {
        Address key = stack().popGet();
        Address arr = stack().popGet();
        int responseCode = arr.contains(key);
        if (isResultFalse(responseCode)) {
            setNextCp(nextCp);
        }
    }

    public void doLinearSwitch(int[] labels, int[] cps, int defaultCp) {
        Address selector = stack().popGet();

        // Не скалярные значения семантически запрещены
        if (!selector.isScalar()) {
            setNextCp(defaultCp);
            return;
        }

        int selectorHash = selector.hashCode();

        for (int i = 0; i < labels.length; i++) {
            Address k = constantPool().getAddressEntry(labels[i]);
            int kHash = k.hashCode();
            if (selectorHash == kHash && selector.fastCompareWith(k, 1) == 0) {
                setNextCp(cps[i]);
                return;
            }
        }
        setNextCp(defaultCp); /* default ip */
    }

    public void doBinarySwitch(int[] labels, int[] cps, int defaultCp) {
        Address selector = stack().popGet();

        // Не скалярные значения семантически запрещены
        if (!selector.isScalar()) {
            setNextCp(defaultCp);
            return;
        }

        int l = 0;
        int h = labels.length - 1;

        while (l <= h) {
            int x = (l + h) >> 1;
            Address k = constantPool().getAddressEntry(labels[x]);
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
        ResolvableCallee callee = constantPool().getCallee(calleeId);
        Function fn;
        if (callee.isResolved()) {
            fn = callee.getResolved();
        } else {
            String name = constantPool().getAddressEntry(callee.getUtf8()).stringVal().toString();
            fn = thread().getEnvironment().lookupFunction(name);
            callee.setResolved(fn);
        }
        thread().prepareCall(fn, argCount);
    }

    public void doReturn() {
        thread().doReturn();
    }

    public void doLeave() {
        thread().leave();
    }
}
