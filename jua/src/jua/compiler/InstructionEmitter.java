package jua.compiler;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import jua.interpreter.Instructions;
import jua.runtime.code.ConstantPool;

import static jua.interpreter.Instructions.*;
import static jua.interpreter.Instructions.OPCodes.*;

public final class InstructionEmitter {

    // todo: Finish functions

    private final LongList instructions = new LongArrayList();

    private ConstantPool.Builder constantPoolBuilder;

    public void nop() {
        emit(NOP);
    }

    public void constNull() {
        emit(CONST_NULL);
    }

    public void constTrue() {
        emit(CONST_TRUE);
    }

    public void constFalse() {
        emit(CONST_FALSE);
    }

    public void push(Object value) {
        if (value == null) {
            constNull();
        } else if (value == Boolean.TRUE) {
            constTrue();
        } else if (value == Boolean.FALSE) {
            constFalse();
        } else {
            Class<?> c = value.getClass();
            int index;
            if (c == Long.class|| c == Integer.class|| c == Short.class || c == Byte.class) {
                long l = ((Number) value).longValue();
                index = constantPoolBuilder.putLongEntry(l);
            } else if (c == Double.class || c == Float.class) {
                double d = ((Number) value).doubleValue();
                index = constantPoolBuilder.putDoubleEntry(d);
            } else if (c == String.class) {
                String s = (String) value;
                index = constantPoolBuilder.putStringEntry(s);
            } else {
                throw new IllegalArgumentException(); // todo: exception message
            }
            emit(PUSH, index);
        }
    }

    public void pop() {
        emit(POP);
    }

    public void pop2() {
        emit(POP2);
    }

    public void dup() {
        emit(DUP);
    }

    public void dupX1() {
        emit(DUP_X1);
    }

    public void dupX2() {
        emit(DUP_X2);
    }

    public void dup2() {
        emit(DUP2);
    }

    public void dup2X1() {
        emit(DUP2_X1);
    }

    public void dup2X2() {
        emit(DUP2_X2);
    }

    public void add() {
        emit(ADD);
    }

    public void sub() {
        emit(SUB);
    }

    public void mul() {
        emit(MUL);
    }

    public void div() {
        emit(DIV);
    }

    public void rem() {
        emit(REM);
    }

    public void shl() {
        emit(SHL);
    }

    public void shr() {
        emit(SHR);
    }

    public void and() {
        emit(AND);
    }

    public void or() {
        emit(OR);
    }

    public void xor() {
        emit(XOR);
    }

    public void neg() {
        emit(NEG);
    }

    public void pos() {
        emit(POS);
    }

    public void not() {
        emit(NOT);
    }

    public void load(int localIndex) {
        if (localIndex <= 3) {
            emit(LOAD_0 + localIndex);
        } else {
            emit(LOAD, localIndex);
        }
    }

    public void store(int localIndex) {
        if (localIndex <= 3) {
            emit(STORE_0 + localIndex);
        } else {
            emit(LOAD, localIndex);
        }
    }

    public void inc() {
        emit(INC);
    }

    public void dec() {
        emit(DEC);
    }

    public void aload() {
        emit(ALOAD);
    }

    public void astore() {
        emit(ASTORE);
    }

    public void ainc() {
        emit(AINC);
    }

    public void adec() {
        emit(ADEC);
    }

    public void length() {
        emit(LENGTH);
    }

    public void clone_() {
        emit(CLONE);
    }

    public void newarray() {
        emit(NEWARRAY);
    }

    public void getconst() {
        emit(GETCONST);
    }

    public void goto_() {
        emit(GOTO);
    }

    public void ifeq(long value, int dstIp) {
        test(IFEQ, value, dstIp);
    }

    public void ifne(long value, int dstIp) {
        test(IFNE, value, dstIp);
    }

    public void ifgt(long value, int dstIp) {
        test(IFGT, value, dstIp);
    }

    public void ifle(long value, int dstIp) {
        test(IFLE, value, dstIp);
    }

    public void iflt(long value, int dstIp) {
        test(IFLT, value, dstIp);
    }

    public void ifge(long value, int dstIp) {
        test(IFGE, value, dstIp);
    }

    private void test(int opcode, long value, int dstIp) {
        if (isSmall(value)) {
            int v = (int) value;
            emit(opcode, v, dstIp);
        } else {
            push(value);
            ifcmpeq(dstIp);
        }
    }

    private boolean isSmall(long value) {
        return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
    }

    public void ifcmpeq(int dstIp) {

    }

    public void ifcmpne(int dstIp) {
    }

    public void ifcmpgt(int dstIp) {
    }

    public void ifcmple(int dstIp) {
    }

    public void ifcmplt(int dstIp) {
    }

    public void ifcmpge(int dstIp) {
    }

    public void ifz() {
        emit(IFZ);
    }

    public void ifnz() {
        emit(IFNZ);
    }

    public void ifnull() {
        emit(IFNULL);
    }

    public void ifnonnull() {
        emit(IFNONNULL);
    }

    public void switch_() {
    }

    public void call(int functionAddress, int numArgs) {
        if (numArgs <= 3) {
            emit(CALL_0 + numArgs, functionAddress);
        } else {
            emit(CALL, functionAddress, numArgs);
        }
    }

    public void return_() {
        emit(RETURN);
    }

    public void halt() {
        emit(HALT);
    }

    private void emit(int opcode) {
        long instruction = 0L;
        instruction = storeOpcode(instruction, opcode);
        instructions.add(instruction);
    }

    private void emit(int opcode, int pa) {
        long instruction = 0L;
        instruction = storeOpcode(instruction, opcode);
        instruction = storePA(instruction, pa);
        instructions.add(instruction);
    }

    private void emit(int opcode, int pa, int pb) {
        long instruction = 0L;
        instruction = storeOpcode(instruction, opcode);
        instruction = storePA(instruction, pa);
        instruction = storePB(instruction, pb);
        instructions.add(instruction);
    }

    private void emit(int opcode, int pa, int pb, int pc) {
        long instruction = 0L;
        instruction = storeOpcode(instruction, opcode);
        instruction = storePA(instruction, pa);
        instruction = storePB(instruction, pb);
        instruction = Instructions.storePC(instruction, pc);
        instructions.add(instruction);
    }
}
