package jua.runtime.interpreter;

import jua.runtime.Function;
import jua.runtime.JuaEnvironment;
import jua.runtime.Types;
import jua.runtime.code.CodeData;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.ResolvableCallee;
import jua.runtime.heap.ListHeap;
import jua.runtime.interpreter.instruction.Instruction;

import static jua.runtime.Operations.isResultFalse;
import static jua.runtime.Operations.isResultTrue;

public final class ExecutionContext {

    private final ThreadStack stack;

    private final ThreadMemory memory;

    private ConstantPool constantPool;

    private int nextCP;

    private int msg = 0;
    private Function msgCallee;
    private int msgArgc = 0;

    private final Address tmp = new Address();

    public ExecutionContext(ThreadStack stack, ThreadMemory memory) {
        this.stack = stack;
        this.memory = memory;
    }

    public ThreadStack getStack() {
        return stack;
    }

    public ThreadMemory getMemory() {
        return memory;
    }

    public void setMsg(int msg) {
        this.msg = msg;
    }

    public int execute(InterpreterFrame frame) {
        msg = 0;
        msgCallee = null;
        msgArgc = 0;

        JuaEnvironment env = JuaEnvironment.getEnvironment();

        CodeData code = env.getFunctionById(frame.getFunctionId()).getCode();
        constantPool = code.getConstantPool();
        Instruction[] instructions = code.getCode();

        memory.setCurrentFrame(frame);
        int cp = frame.getCP();
        while (true) {
            nextCP = cp + 1;
//            Histogram.get().start(instructions[cp].opcode());
            instructions[cp].execute(this);
//            Histogram.get().end(instructions[cp].opcode());
            cp = nextCP;
            if (msg != 0) {
                if (msg != InterpreterThread.MSG_CRASHED) {
                    frame.setCP(cp);
                }
                break;
            }
            frame.setCP(cp);
        }

        constantPool = null;
        return msg;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public int getNextCp() {
        return nextCP;
    }

    public void setNextCp(int nextCp) {
        this.nextCP = nextCp;
    }

    public int getMsg() {
        return msg;
    }

    public Function getMsgCallee() {
        return msgCallee;
    }

    public int getMsgArgc() {
        return msgArgc;
    }

    /*
     * =======================================================
     * ===============> РЕАЛИЗАЦИИ ИНСТРУКЦИЙ <===============
     * =======================================================
     *
     */

    public void doConstInt(long value) {
        getStack().pushGet().set(value);
    }

    public void doConstFalse() {
        getStack().pushGet().set(false);
    }

    public void doConstTrue() {
        getStack().pushGet().set(true);
    }

    public void doConstNull() {
        getStack().pushGet().setNull();
    }

    public void doPush(int cpi) { // Constant Pool Index
        getConstantPool().load(cpi, getStack().pushGet());
    }

    public void doDup() {
        getStack().dup();
    }

    public void doDup2() {
        getStack().dup2();
    }

    public void doDupX1() {
        getStack().dupX1();
    }

    public void doDupX2() {
        getStack().dupX2();
    }

    public void doDup2x1() {
        getStack().dup2X1();
    }

    public void doDup2x2() {
        getStack().dup2X2();
    }

    public void doPop() {
        getStack().pop();
    }

    public void doPop2() {
        getStack().pop2();
    }

    public void doAdd() {
        Address rhs = getStack().popGet();
        Address lhs = getStack().popGet();
        lhs.add(rhs, lhs);
        getStack().push(lhs);
    }

    public void doSub() {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        lhs.sub(rhs, lhs);
        getStack().subTos(1);
    }

    public void doDiv() {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        lhs.div(rhs, lhs);
        getStack().subTos(1);
    }

    public void doMul() {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        lhs.mul(rhs, lhs);
        getStack().subTos(1);
    }

    public void doRem() {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        lhs.rem(rhs, lhs);
        getStack().subTos(1);
    }

    public void doAnd() {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        lhs.and(rhs, lhs);
        getStack().subTos(1);
    }

    public void doOr() {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        lhs.or(rhs, lhs);
        getStack().subTos(1);
    }

    public void doXor() {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        lhs.xor(rhs, lhs);
        getStack().subTos(1);
    }

    public void doShl() {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        lhs.shl(rhs, lhs);
        getStack().subTos(1);
    }

    public void doShr() {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        lhs.shr(rhs, lhs);
        getStack().subTos(1);
    }

    public void doPos() {
        Address value = getStack().getStackAddressBack(1);
        value.pos(value);
    }

    public void doNeg() {
        Address value = getStack().getStackAddressBack(1);
        value.neg(value);
    }

    public void doNot() {
        Address value = getStack().getStackAddressBack(1);
        value.not(value);
    }

    public void doLength() {
        Address value = getStack().getStackAddressBack(1);
        value.length(value);
    }

    public void doLoad(int i) {
        getStack().push(getMemory().get(i));
    }

    public void doStore(int i) {
        getMemory().get(i).set(getStack().getStackAddressBack(1));
        getStack().subTos(1);
    }

    public void doInc(int i) {
        getMemory().get(i).inc();
    }

    public void doDec(int i) {
        getMemory().get(i).dec();
    }

    public void doArrayLoad() {
        Address arr = getStack().getStackAddressBack(2);
        Address key = getStack().getStackAddressBack(1);
        arr.load(key, arr);
        getStack().subTos(1);
    }

    public void doArrayStore() {
        Address arr = getStack().getStackAddressBack(3);
        Address key = getStack().getStackAddressBack(2);
        Address val = getStack().getStackAddressBack(1);
        arr.store(key, val);
        getStack().subTos(3);
    }

    public void doArrayInc() {
        Address arr = getStack().getStackAddressBack(2);
        Address key = getStack().getStackAddressBack(1);
        arr.arrayInc(key, arr);
        getStack().subTos(1);
    }

    public void doArrayDec() {
        Address arr = getStack().getStackAddressBack(2);
        Address key = getStack().getStackAddressBack(1);
        arr.arrayDec(key, arr);
        getStack().subTos(1);
    }

    public void doNewList() {
        Address value = getStack().getStackAddressBack(1);
        long a;
        if (!value.hasType(Types.T_INT) ||
                (a = value.getLong()) < 0 ||
                Integer.MAX_VALUE < a) {
            InterpreterThread.currentThread().error("List size must be an unsigned 32-bit integer");
            return;
        }
        value.set(new ListHeap((int) a));
    }

    public void doJumpIfEq(int nextCp) {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        getStack().subTos(2);
        if (lhs.fastCompareWith(rhs, 1) == 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfntEq(int thenCp) {
        Address rhs = getStack().popGet();
        Address lhs = getStack().popGet();
        if (lhs.fastCompareWith(rhs, 1) != 0)
            setNextCp(thenCp);
    }

    public void doJumpIfGt(int nextCp) {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        getStack().subTos(2);
        if (lhs.fastCompareWith(rhs, -1) > 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfGe(int nextCp) {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        getStack().subTos(2);
        if (lhs.fastCompareWith(rhs, -1) >= 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfLt(int nextCp) {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        getStack().subTos(2);
        if (lhs.fastCompareWith(rhs, 1) < 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfLe(int nextCp) {
        Address lhs = getStack().getStackAddressBack(2);
        Address rhs = getStack().getStackAddressBack(1);
        getStack().subTos(2);
        if (lhs.fastCompareWith(rhs, 1) <= 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfNull(int nextCp) {
        Address value = getStack().getStackAddressBack(1);
        getStack().subTos(1);
        if (value.isNull()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfNonZero(int nextCp) {
        Address value = getStack().getStackAddressBack(1);
        getStack().subTos(1);
        if (value.booleanVal()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfZero(int nextCp) {
        Address value = getStack().getStackAddressBack(1);
        getStack().subTos(1);
        if (!value.booleanVal()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfntNull(int nextCp) {
        Address value = getStack().getStackAddressBack(1);
        getStack().subTos(1);
        if (!value.isNull()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfPresent(int nextCp) {
        Address key = getStack().popGet();
        Address arr = getStack().popGet();
        int responseCode = arr.contains(key);
        if (isResultTrue(responseCode)) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfAbsent(int nextCp) {
        Address key = getStack().popGet();
        Address arr = getStack().popGet();
        int responseCode = arr.contains(key);
        if (isResultFalse(responseCode)) {
            setNextCp(nextCp);
        }
    }

    public void doLinearSwitch(int[] labels, int[] cps, int defaultCp) {
        Address selector = getStack().popGet();

        // Не скалярные значения семантически запрещены
        if (!selector.isScalar()) {
            setNextCp(defaultCp);
            return;
        }

        int selectorHash = selector.hashCode();

        for (int i = 0; i < labels.length; i++) {
            getConstantPool().load(labels[i], tmp);
            int kHash = tmp.hashCode();
            if (selectorHash == kHash && selector.fastCompareWith(tmp, 1) == 0) {
                setNextCp(cps[i]);
                return;
            }
        }
        setNextCp(defaultCp); /* default ip */
    }

    public void doBinarySwitch(int[] labels, int[] cps, int defaultCp) {
        Address selector = getStack().popGet();

        // Не скалярные значения семантически запрещены
        if (!selector.isScalar()) {
            setNextCp(defaultCp);
            return;
        }

        int l = 0;
        int h = labels.length - 1;

        while (l <= h) {
            int x = (l + h) >> 1;
            getConstantPool().load(labels[x], tmp);
            int d = selector.compareTo(tmp);

            if (d > 0) {
                l = x + 1;
            } else if (d < 0) {
                h = x - 1;
            } else {
                // Не помню, почему d не должно равняться 2, но удалять не буду — вдруг что-то важное.
                /* assert d != 2; */

                // Если selector != k, значит один из операндов это NaN и цикл все равно завершен.
                if (selector.fastCompareWith(tmp, 1) == 0) {
                    setNextCp(cps[x]);
                }
                return;
            }
        }

        setNextCp(defaultCp); /* default offset */
    }

    public void doCall(int calleeId, int argCount) {
//        Histogram.get().start(OPCodes._JoinNativeFrame);
//        Histogram.get().start(OPCodes._JoinFrame);
        ResolvableCallee callee = getConstantPool().getCallee(calleeId);
        Function fn;
        if (callee.isResolved()) {
            fn = callee.getResolved();
        } else {
            String name = getConstantPool().getUtf8(callee.getUtf8());
            fn = JuaEnvironment.getEnvironment().lookupFunction(name);
            callee.setResolved(fn);
        }

        msg = InterpreterThread.MSG_CALLING_FRAME;
        msgCallee = fn;
        msgArgc = argCount;
    }

    public void doReturn() {
        msg = InterpreterThread.MSG_POPPING_FRAME;
//        Histogram.get().start(OPCodes._PopFrame);
    }

    public void doLeave() {
        getStack().pushGet().setNull();
        msg = InterpreterThread.MSG_POPPING_FRAME;
//        Histogram.get().start(OPCodes._PopFrame);
    }

    public void shareLoad(int index) {
        getStack().push(memory.getShared(index));
    }

    public void shareStore(int index) {
        memory.getShared(index).set(getStack().popGet());
    }
}
