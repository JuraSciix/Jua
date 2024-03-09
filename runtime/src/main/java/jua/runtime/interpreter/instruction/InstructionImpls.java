package jua.runtime.interpreter.instruction;

import jua.runtime.interpreter.ExecutionContext;

import static jua.runtime.interpreter.OPCodes.*;

public interface InstructionImpls {

    class Nop implements Instruction {
        @Override
        public void execute(ExecutionContext context) {}

        @Override
        public int opcode() { return Nop; }
    }

    class ConstIntM1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) {
            context.doConstInt(-1);
        }

        @Override
        public int opcode() { return ConstIntM1; }
    }

    class ConstInt0 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstInt(0); }

        @Override
        public int opcode() { return ConstInt0; }
    }

    class ConstInt1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstInt(1); }

        @Override
        public int opcode() { return ConstInt1; }
    }

    class ConstInt2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstInt(2); }

        @Override
        public int opcode() { return ConstInt2; }
    }

    class ConstFalse implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstFalse(); }

        @Override
        public int opcode() { return ConstFalse; }
    }

    class ConstTrue implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstTrue(); }

        @Override
        public int opcode() { return ConstTrue; }
    }

    class ConstNull implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstNull(); }

        @Override
        public int opcode() { return ConstNull; }
    }

    class Push implements Instruction {
        private final int cpi;

        public Push(int cpi) {
            this.cpi = cpi;
        }

        @Override
        public void execute(ExecutionContext context) { context.doPush(cpi); }

        @Override
        public int opcode() { return Push; }
    }

    class Dup implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup(); }

        @Override
        public int opcode() { return Dup; }
    }

    class DupX1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDupX1(); }

        @Override
        public int opcode() { return DupX1; }
    }

    class DupX2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDupX2(); }

        @Override
        public int opcode() { return DupX2; }
    }

    class Dup2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup2(); }

        @Override
        public int opcode() { return Dup2; }
    }

    class Dup2X1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup2x1(); }

        @Override
        public int opcode() { return Dup2X1; }
    }

    class Dup2X2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup2x2(); }

        @Override
        public int opcode() { return Dup2X2; }
    }

    class Pop implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doPop(); }

        @Override
        public int opcode() { return Pop; }
    }

    class Pop2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doPop2(); }

        @Override
        public int opcode() { return Pop2; }
    }

    class Add implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doAdd(); }

        @Override
        public int opcode() { return Add; }
    }

    class Sub implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doSub(); }

        @Override
        public int opcode() { return Sub; }
    }

    class Mul implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doMul(); }

        @Override
        public int opcode() { return Mul; }
    }

    class Div implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDiv(); }

        @Override
        public int opcode() { return Div; }
    }

    class Rem implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doRem(); }

        @Override
        public int opcode() { return Rem; }
    }

    class Shl implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doShl(); }

        @Override
        public int opcode() { return Shl; }
    }

    class Shr implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doShr(); }

        @Override
        public int opcode() { return Shr; }
    }

    class And implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doAnd(); }

        @Override
        public int opcode() { return And; }
    }

    class Or implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doOr(); }

        @Override
        public int opcode() { return Or; }
    }

    class Xor implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doXor(); }

        @Override
        public int opcode() { return Xor; }
    }

    class Pos implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doPos(); }

        @Override
        public int opcode() { return Pos; }
    }

    class Neg implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doNeg(); }

        @Override
        public int opcode() { return Neg; }
    }

    class Not implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doNot(); }

        @Override
        public int opcode() { return Not; }
    }

    class Length implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLength(); }

        @Override
        public int opcode() { return Length; }
    }

    class Load0 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLoad(0); }

        @Override
        public int opcode() { return Load0; }
    }

    class Load1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLoad(1); }

        @Override
        public int opcode() { return Load1; }
    }

    class Load2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLoad(2); }

        @Override
        public int opcode() { return Load2; }
    }

    class Load implements Instruction {
        private final int i; // Local ID

        public Load(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doLoad(i); }

        @Override
        public int opcode() { return Load; }
    }

    class Store0 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doStore(0); }

        @Override
        public int opcode() { return Store0; }
    }

    class Store1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doStore(1); }

        @Override
        public int opcode() { return Store1; }
    }

    class Store2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doStore(2); }

        @Override
        public int opcode() { return Store2; }
    }

    class Store implements Instruction {
        private final int i; // Local ID

        public Store(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doStore(i); }

        @Override
        public int opcode() { return Store; }
    }

    class Inc implements Instruction {
        private final int i; // Local ID

        public Inc(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doInc(i); }

        @Override
        public int opcode() { return Inc; }
    }

    class Dec implements Instruction {
        private final int i; // Local ID

        public Dec(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doDec(i); }

        @Override
        public int opcode() { return Dec; }
    }

    class ArrayLoad implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayLoad(); }

        @Override
        public int opcode() { return ArrayLoad; }
    }

    class ArrayStore implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayStore(); }

        @Override
        public int opcode() { return ArrayStore; }
    }

    class ArrayInc implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayInc(); }

        @Override
        public int opcode() { return ArrayInc; }
    }

    class ArrayDec implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayDec(); }

        @Override
        public int opcode() { return ArrayDec; }
    }

    class NewList implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doNewList(); }

        @Override
        public int opcode() { return NewList; }
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
        public int opcode() { return Goto; }
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
        public int opcode() { return IfEq; }
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
        public int opcode() { return IfNe; }
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
        public int opcode() { return IfGt; }
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
        public int opcode() { return IfLe; }
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
        public int opcode() { return IfLt; }
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
        public int opcode() { return IfGe; }
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
        public int opcode() { return IfNz; }
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
        public int opcode() { return IfZ; }
    }

    class IfNull extends JumpInstruction {
        public IfNull(int thenCp) {
            super(thenCp);
        }

        @Override
        public int opcode() { return IfNull; }

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
        public int opcode() { return IfNonNull; }
    }

    class IfPresent extends JumpInstruction {
        public IfPresent(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfPresent(getNextCp());
        }

        @Override
        public int opcode() { return IfPresent; }
    }

    class IfAbsent extends JumpInstruction {
        public IfAbsent(int thenCp) {
            super(thenCp);
        }
        
        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfAbsent(getNextCp());
        }

        @Override
        public int opcode() { return IfAbsent; }
    }

    class LinearSwitch implements Instruction {
        private final int[] labels;
        private final int[] cps;
        private final int defaultCp;

        public LinearSwitch(int[] labels, int[] cps, int defaultCp) {
            this.labels = labels;
            this.cps = cps;
            this.defaultCp = defaultCp;
        }
        
        @Override
        public void execute(ExecutionContext context) { context.doLinearSwitch(labels, cps, defaultCp); }

        @Override
        public int opcode() { return LinearSwitch; }
    }

    class BinarySwitch implements Instruction {
        private final int[] labels;
        private final int[] cps;
        private final int defaultCp;

        public BinarySwitch(int[] labels, int[] cps, int defaultCp) {
            this.labels = labels;
            this.cps = cps;
            this.defaultCp = defaultCp;
        }

        @Override
        public void execute(ExecutionContext context) { context.doBinarySwitch(labels, cps, defaultCp); }

        @Override
        public int opcode() { return BinarySwitch; }
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
        public int opcode() { return Call; }
    }

    class Return implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doReturn(); }

        @Override
        public int opcode() { return Return; }
    }

    class Leave implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLeave(); }

        @Override
        public int opcode() { return Leave; }
    }
}
