package jua.vm;

import jua.runtime.Function;
import jua.runtime.JuaEnvironment;
import jua.runtime.Types;
import jua.runtime.code.CodeData;
import jua.runtime.code.ConstantPool;
import jua.runtime.code.ResolvableCallee;
import jua.runtime.heap.ListHeap;
import jua.vm.instruction.Instruction;

public final class ExecutionContext {

    private final ThreadStack stack;

    private final ThreadRegion memory;

    private ConstantPool constantPool;

    private int nextCP;

    private int msg = 0;
    private Function msgCallee;
    private int msgArgc = 0;

    private final Address tmp = new Address();

    public ExecutionContext(ThreadStack stack, ThreadRegion memory) {
        this.stack = stack;
        this.memory = memory;
    }

    public ThreadStack getStack() {
        return stack;
    }

    public ThreadRegion getMemory() {
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
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        lhs.sub(rhs, lhs);
        getStack().decrement();
    }

    public void doDiv() {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        lhs.div(rhs, lhs);
        getStack().decrement();
    }

    public void doMul() {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        lhs.mul(rhs, lhs);
        getStack().decrement();
    }

    public void doRem() {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        lhs.rem(rhs, lhs);
        getStack().decrement();
    }

    public void doAnd() {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        lhs.and(rhs, lhs);
        getStack().decrement();
    }

    public void doOr() {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        lhs.or(rhs, lhs);
        getStack().decrement();
    }

    public void doXor() {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        lhs.xor(rhs, lhs);
        getStack().decrement();
    }

    public void doShl() {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        lhs.shl(rhs, lhs);
        getStack().decrement();
    }

    public void doShr() {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        lhs.shr(rhs, lhs);
        getStack().decrement();
    }

    public void doPos() {
        Address value = getStack().peek1();
        value.pos(value);
    }

    public void doNeg() {
        Address value = getStack().peek1();
        value.neg(value);
    }

    public void doNot() {
        Address value = getStack().peek1();
        value.not(value);
    }

    public void doLength() {
        Address value = getStack().peek1();
        value.length(value);
    }

    public void doLoad(int i) {
        getStack().push(getMemory().registry(i));
    }

    public void doStore(int i) {
        getMemory().registry(i).set(getStack().peek1());
        getStack().decrement();
    }

    public void doInc(int i) {
        getMemory().registry(i).inc();
    }

    public void doDec(int i) {
        getMemory().registry(i).dec();
    }

    public void doArrayLoad() {
        Address arr = getStack().peek2();
        Address key = getStack().peek1();
        arr.load(key, arr);
        getStack().decrement();
    }

    public void doArrayStore() {
        Address arr = getStack().peek3();
        Address key = getStack().peek2();
        Address val = getStack().peek1();
        arr.store(key, val);
        getStack().decrement3();
    }

    public void doArrayInc() {
        Address arr = getStack().peek2();
        Address key = getStack().peek1();
        arr.arrayInc(key, arr);
        getStack().decrement();
    }

    public void doArrayDec() {
        Address arr = getStack().peek2();
        Address key = getStack().peek1();
        arr.arrayDec(key, arr);
        getStack().decrement();
    }

    public void doNewList() {
        Address value = getStack().peek1();
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
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        getStack().decrement2();
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
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        getStack().decrement2();
        if (lhs.fastCompareWith(rhs, -1) > 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfGe(int nextCp) {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        getStack().decrement2();
        if (lhs.fastCompareWith(rhs, -1) >= 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfLt(int nextCp) {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        getStack().decrement2();
        if (lhs.fastCompareWith(rhs, 1) < 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfLe(int nextCp) {
        Address lhs = getStack().peek2();
        Address rhs = getStack().peek1();
        getStack().decrement2();
        if (lhs.fastCompareWith(rhs, 1) <= 0) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfNull(int nextCp) {
        Address value = getStack().peek1();
        getStack().decrement();
        if (value.isNull()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfNonZero(int nextCp) {
        Address value = getStack().peek1();
        getStack().decrement();
        if (value.booleanVal()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfZero(int nextCp) {
        Address value = getStack().peek1();
        getStack().decrement();
        if (!value.booleanVal()) {
            setNextCp(nextCp);
        }
    }

    public void doJumpIfntNull(int nextCp) {
        Address value = getStack().peek1();
        getStack().decrement();
        if (!value.isNull()) {
            setNextCp(nextCp);
        }
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
}
