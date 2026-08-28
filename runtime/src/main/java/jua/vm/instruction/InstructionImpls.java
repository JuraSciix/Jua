package jua.vm.instruction;

import jua.vm.ExecutionContext;

import static jua.vm.OPCodes.*;

public interface InstructionImpls {

    class Nop implements Instruction {
        @Override
        public void execute(ExecutionContext context) {}

        @Override
        public int opcode() { return _nop; }
    }

    class ConstIntM1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) {
            context.doConstInt(-1);
        }

        @Override
        public int opcode() { return _const_i_m1; }
    }

    class ConstInt0 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstInt(0); }

        @Override
        public int opcode() { return _const_i_0; }
    }

    class ConstInt1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstInt(1); }

        @Override
        public int opcode() { return _const_i_1; }
    }

    class ConstInt2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstInt(2); }

        @Override
        public int opcode() { return _const_i_2; }
    }

    class ConstFalse implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstFalse(); }

        @Override
        public int opcode() { return _const_false; }
    }

    class ConstTrue implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstTrue(); }

        @Override
        public int opcode() { return _const_true; }
    }

    class ConstNull implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstNull(); }

        @Override
        public int opcode() { return _const_null; }
    }

    class Push implements Instruction {
        private final int cpi;

        public Push(int cpi) {
            this.cpi = cpi;
        }

        @Override
        public void execute(ExecutionContext context) { context.doPush(cpi); }

        @Override
        public int opcode() { return _push; }
    }

    class Dup implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup(); }

        @Override
        public int opcode() { return _dup; }
    }

    class DupX1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDupX1(); }

        @Override
        public int opcode() { return _dup_x1; }
    }

    class DupX2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDupX2(); }

        @Override
        public int opcode() { return _dup_x2; }
    }

    class Dup2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup2(); }

        @Override
        public int opcode() { return _dup2; }
    }

    class Dup2X1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup2x1(); }

        @Override
        public int opcode() { return _dup2_x1; }
    }

    class Dup2X2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup2x2(); }

        @Override
        public int opcode() { return _dup2_x2; }
    }

    class Pop implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doPop(); }

        @Override
        public int opcode() { return _pop; }
    }

    class Pop2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doPop2(); }

        @Override
        public int opcode() { return _pop2; }
    }

    class Add implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doAdd(); }

        @Override
        public int opcode() { return _add; }
    }

    class Sub implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doSub(); }

        @Override
        public int opcode() { return _sub; }
    }

    class Mul implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doMul(); }

        @Override
        public int opcode() { return _mul; }
    }

    class Div implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDiv(); }

        @Override
        public int opcode() { return _div; }
    }

    class Rem implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doRem(); }

        @Override
        public int opcode() { return _rem; }
    }

    class Shl implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doShl(); }

        @Override
        public int opcode() { return _bit_shl; }
    }

    class Shr implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doShr(); }

        @Override
        public int opcode() { return _bit_shr; }
    }

    class And implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doAnd(); }

        @Override
        public int opcode() { return _bit_and; }
    }

    class Or implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doOr(); }

        @Override
        public int opcode() { return _bit_or; }
    }

    class Xor implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doXor(); }

        @Override
        public int opcode() { return _bit_xor; }
    }

    class Pos implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doPos(); }

        @Override
        public int opcode() { return _pos; }
    }

    class Neg implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doNeg(); }

        @Override
        public int opcode() { return _neg; }
    }

    class Not implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doNot(); }

        @Override
        public int opcode() { return _bit_inv; }
    }

    class Length implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLength(); }

        @Override
        public int opcode() { return _len; }
    }

    class Load0 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLoad(0); }

        @Override
        public int opcode() { return _load0; }
    }

    class Load1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLoad(1); }

        @Override
        public int opcode() { return _load1; }
    }

    class Load2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLoad(2); }

        @Override
        public int opcode() { return _load2; }
    }

    class Load implements Instruction {
        private final int i; // Local ID

        public Load(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doLoad(i); }

        @Override
        public int opcode() { return _load; }
    }

    class Store0 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doStore(0); }

        @Override
        public int opcode() { return _store0; }
    }

    class Store1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doStore(1); }

        @Override
        public int opcode() { return _store1; }
    }

    class Store2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doStore(2); }

        @Override
        public int opcode() { return _store2; }
    }

    class Store implements Instruction {
        private final int i; // Local ID

        public Store(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doStore(i); }

        @Override
        public int opcode() { return _store; }
    }

    class Inc implements Instruction {
        private final int i; // Local ID

        public Inc(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doInc(i); }

        @Override
        public int opcode() { return _inc; }
    }

    class Dec implements Instruction {
        private final int i; // Local ID

        public Dec(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doDec(i); }

        @Override
        public int opcode() { return _dec; }
    }

    class ArrayLoad implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayLoad(); }

        @Override
        public int opcode() { return _a_load; }
    }

    class ArrayStore implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayStore(); }

        @Override
        public int opcode() { return _a_store; }
    }

    class ArrayInc implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayInc(); }

        @Override
        public int opcode() { return _a_inc; }
    }

    class ArrayDec implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayDec(); }

        @Override
        public int opcode() { return _a_dec; }
    }

    class NewList implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doNewList(); }

        @Override
        public int opcode() { return _new_list; }
    }

    abstract class JumpInstruction implements Instruction {
        private final int nextCp;

        protected JumpInstruction(int nextCp) {
            this.nextCp = nextCp;
        }

        public int getNextCp() {
            return nextCp;
        }
    }

    class Goto extends JumpInstruction {
        public Goto(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) { context.setNextCp(getNextCp()); }

        @Override
        public int opcode() { return _goto; }
    }

    class IfEq extends JumpInstruction {
        public IfEq(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfEq(getNextCp());
        }

        @Override
        public int opcode() { return _if_eq; }
    }

    class IfNe extends JumpInstruction {
        public IfNe(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfntEq(getNextCp());
        }

        @Override
        public int opcode() { return _if_ne; }
    }

    class IfGt extends JumpInstruction {
        public IfGt(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfGt(getNextCp());
        }

        @Override
        public int opcode() { return _if_gt; }
    }

    class IfLe extends JumpInstruction {
        public IfLe(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfLe(getNextCp());
        }

        @Override
        public int opcode() { return _if_le; }
    }

    class IfLt extends JumpInstruction {
        public IfLt(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfLt(getNextCp());
        }

        @Override
        public int opcode() { return _if_lt; }
    }

    class IfGe extends JumpInstruction {
        public IfGe(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfGe(getNextCp());
        }

        @Override
        public int opcode() { return _if_ge; }
    }

    class IfNz extends JumpInstruction {
        public IfNz(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfNonZero(getNextCp());
        }

        @Override
        public int opcode() { return _if_nz; }
    }

    class IfZ extends JumpInstruction {
        public IfZ(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfZero(getNextCp());
        }

        @Override
        public int opcode() { return _if_z; }
    }

    class IfNull extends JumpInstruction {
        public IfNull(int thenCp) {
            super(thenCp);
        }

        @Override
        public int opcode() { return _if_n; }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfNull(getNextCp());
        }
    }

    class IfNonNull extends JumpInstruction {
        public IfNonNull(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfntNull(getNextCp());
        }

        @Override
        public int opcode() { return _if_nn; }
    }

    class Call implements Instruction {
        private final int calleeId;
        private final int argCount;

        public Call(int calleeId, int argCount) {
            this.calleeId = calleeId;
            this.argCount = argCount;
        }

        @Override
        public void execute(ExecutionContext context) { context.doCall(calleeId, argCount); }

        @Override
        public int opcode() { return _call; }
    }

    class Return implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doReturn(); }

        @Override
        public int opcode() { return _return; }
    }

    class Leave implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLeave(); }

        @Override
        public int opcode() { return _leave; }
    }
}
