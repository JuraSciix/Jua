package jua.runtime.interpreter.instruction;

import jua.runtime.interpreter.ExecutionContext;

public interface InstructionImpls {

    class Nop implements Instruction {
        @Override
        public void execute(ExecutionContext context) {}
    }

    class ConstIntM1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) {
            context.doConstInt(-1);
        }
    }

    class ConstInt0 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstInt(0); }
    }

    class ConstInt1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstInt(1); }
    }

    class ConstInt2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstInt(2); }
    }

    class ConstFalse implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstFalse(); }
    }

    class ConstTrue implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstTrue(); }
    }

    class ConstNull implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doConstNull(); }
    }

    class Push implements Instruction {
        private final int cpi;

        public Push(int cpi) {
            this.cpi = cpi;
        }

        @Override
        public void execute(ExecutionContext context) { context.doPush(cpi); }
    }

    class Dup implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup(); }
    }

    class DupX1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDupX1(); }
    }

    class DupX2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDupX2(); }
    }

    class Dup2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup2(); }
    }

    class Dup2X1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup2x1(); }
    }

    class Dup2X2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDup2x2(); }
    }

    class Pop implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doPop(); }
    }

    class Pop2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doPop2(); }
    }

    class Add implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doAdd(); }
    }

    class Sub implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doSub(); }
    }

    class Mul implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doMul(); }
    }

    class Div implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doDiv(); }
    }

    class Rem implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doRem(); }
    }

    class Shl implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doShl(); }
    }

    class Shr implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doShr(); }
    }

    class And implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doAnd(); }
    }

    class Or implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doOr(); }
    }

    class Xor implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doXor(); }
    }

    class Pos implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doPos(); }
    }

    class Neg implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doNeg(); }
    }

    class Not implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doNot(); }
    }

    class Length implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLength(); }
    }

    class Load0 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLoad(0); }
    }

    class Load1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLoad(1); }
    }

    class Load2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLoad(2); }
    }

    class Load implements Instruction {
        private final int i; // Local ID

        public Load(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doLoad(i); }
    }

    class Store0 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doStore(0); }
    }

    class Store1 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doStore(1); }
    }

    class Store2 implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doStore(2); }
    }

    class Store implements Instruction {
        private final int i; // Local ID

        public Store(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doStore(i); }
    }

    class Inc implements Instruction {
        private final int i; // Local ID

        public Inc(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doInc(i); }
    }

    class Dec implements Instruction {
        private final int i; // Local ID

        public Dec(int i) {
            this.i = i;
        }

        @Override
        public void execute(ExecutionContext context) { context.doDec(i); }
    }

    class ArrayLoad implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayLoad(); }
    }

    class ArrayStore implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayStore(); }
    }

    class ArrayInc implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayInc(); }
    }

    class ArrayDec implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doArrayDec(); }
    }

    class NewList implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doNewList(); }
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
    }

    class IfEq extends JumpInstruction {
        public IfEq(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfEq(getNextCp());
        }
    }

    class IfNe extends JumpInstruction {
        public IfNe(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfntEq(getNextCp());
        }
    }

    class IfGt extends JumpInstruction {
        public IfGt(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfGt(getNextCp());
        }
    }

    class IfLe extends JumpInstruction {
        public IfLe(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfLe(getNextCp());
        }
    }

    class IfLt extends JumpInstruction {
        public IfLt(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfLt(getNextCp());
        }
    }

    class IfGe extends JumpInstruction {
        public IfGe(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfGe(getNextCp());
        }
    }

    class IfNz extends JumpInstruction {
        public IfNz(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfNonZero(getNextCp());
        }
    }

    class IfZ extends JumpInstruction {
        public IfZ(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfZero(getNextCp());
        }
    }

    class IfNull extends JumpInstruction {
        public IfNull(int thenCp) {
            super(thenCp);
        }

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
    }

    class IfPresent extends JumpInstruction {
        public IfPresent(int thenCp) {
            super(thenCp);
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfPresent(getNextCp());
        }
    }

    class IfAbsent extends JumpInstruction {
        public IfAbsent(int thenCp) {
            super(thenCp);
        }
        
        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfAbsent(getNextCp());
        }
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
    }

    class Return implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doReturn(); }
    }

    class Leave implements Instruction {
        @Override
        public void execute(ExecutionContext context) { context.doLeave(); }
    }
}
