package jua.interpreter.instruction;

import jua.interpreter.ExecutionContext;
import jua.interpreter.address.Address;
import jua.runtime.code.ConstantPool;

import java.util.Arrays;

public interface InstructionImpls {

    class Nop implements Instruction {
        @Override
        public int stackAdjustment() { return 0; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("nop");
        }

        @Override
        public void execute(ExecutionContext context) {}
    }

    class ConstIntM1 implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("const_im1");
        }

        @Override
        public void execute(ExecutionContext context) {
            context.doConstInt(-1);
        }
    }

    class ConstInt0 implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("const_i0");
        }

        @Override
        public void execute(ExecutionContext context) { context.doConstInt(0); }
    }

    class ConstInt1 implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("const_i1");
        }

        @Override
        public void execute(ExecutionContext context) { context.doConstInt(1); }
    }

    class ConstInt2 implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("const_i2");
        }

        @Override
        public void execute(ExecutionContext context) { context.doConstInt(2); }
    }

    class ConstFalse implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("const_false");
        }

        @Override
        public void execute(ExecutionContext context) { context.doConstFalse(); }
    }

    class ConstTrue implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("const_true");
        }

        @Override
        public void execute(ExecutionContext context) { context.doConstTrue(); }
    }

    class ConstNull implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("const_null");
        }

        @Override
        public void execute(ExecutionContext context) { context.doConstNull(); }
    }

    class Push implements Instruction {
        private final int cpi;

        public Push(int cpi) {
            this.cpi = cpi;
        }

        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("push");
            printer.printLiteral(cpi);
        }

        @Override
        public void execute(ExecutionContext context) { context.doPush(cpi); }
    }

    class Dup implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("dup");
        }

        @Override
        public void execute(ExecutionContext context) { context.doDup(); }
    }

    class DupX1 implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("dup_x1");
        }

        @Override
        public void execute(ExecutionContext context) { context.doDupX1(); }
    }

    class DupX2 implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("dup_x2");
        }

        @Override
        public void execute(ExecutionContext context) { context.doDupX2(); }
    }

    class Dup2 implements Instruction {
        @Override
        public int stackAdjustment() { return 2; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("dup2");
        }

        @Override
        public void execute(ExecutionContext context) { context.doDup2(); }
    }

    class Dup2X1 implements Instruction {
        @Override
        public int stackAdjustment() { return 2; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("dup2_x1");
        }

        @Override
        public void execute(ExecutionContext context) { context.doDup2x1(); }
    }

    class Dup2X2 implements Instruction {
        @Override
        public int stackAdjustment() { return 2; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("dup2_x2");
        }

        @Override
        public void execute(ExecutionContext context) { context.doDup2x2(); }
    }

    class Pop implements Instruction {
        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("pop");
        }

        @Override
        public void execute(ExecutionContext context) { context.doPop(); }
    }

    class Pop2 implements Instruction {
        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("pop2");
        }

        @Override
        public void execute(ExecutionContext context) { context.doPop2(); }
    }

    class Add implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("add");
        }

        @Override
        public void execute(ExecutionContext context) { context.doAdd(); }
    }

    class Sub implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("sub");
        }

        @Override
        public void execute(ExecutionContext context) { context.doSub(); }
    }

    class Mul implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("mul");
        }

        @Override
        public void execute(ExecutionContext context) { context.doMul(); }
    }

    class Div implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("div");
        }

        @Override
        public void execute(ExecutionContext context) { context.doDiv(); }
    }

    class Rem implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("rem");
        }

        @Override
        public void execute(ExecutionContext context) { context.doRem(); }
    }

    class Shl implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("shl");
        }

        @Override
        public void execute(ExecutionContext context) { context.doShl(); }
    }

    class Shr implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("shr");
        }

        @Override
        public void execute(ExecutionContext context) { context.doShr(); }
    }

    class And implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("and");
        }

        @Override
        public void execute(ExecutionContext context) { context.doAnd(); }
    }

    class Or implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("or");
        }

        @Override
        public void execute(ExecutionContext context) { context.doOr(); }
    }

    class Xor implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("xor");
        }

        @Override
        public void execute(ExecutionContext context) { context.doXor(); }
    }

    class Pos implements Instruction {
        @Override
        public int stackAdjustment() { return -1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("pos");
        }

        @Override
        public void execute(ExecutionContext context) { context.doPos(); }
    }

    class Neg implements Instruction {
        @Override
        public int stackAdjustment() { return -1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("neg");
        }

        @Override
        public void execute(ExecutionContext context) { context.doNeg(); }
    }

    class Not implements Instruction {
        @Override
        public int stackAdjustment() { return -1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("not");
        }

        @Override
        public void execute(ExecutionContext context) { context.doNot(); }
    }

    class Length implements Instruction {
        @Override
        public int stackAdjustment() { return -1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("length");
        }

        @Override
        public void execute(ExecutionContext context) { context.doLength(); }
    }

    class Load0 implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("load_0");
            printer.printLocal(0);
        }

        @Override
        public void execute(ExecutionContext context) { context.doLoad(0); }
    }

    class Load1 implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("load_1");
            printer.printLocal(1);
        }

        @Override
        public void execute(ExecutionContext context) { context.doLoad(1); }
    }

    class Load2 implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("load_2");
            printer.printLocal(2);
        }

        @Override
        public void execute(ExecutionContext context) { context.doLoad(2); }
    }

    class Load implements Instruction {
        private final int i; // Local ID

        public Load(int i) {
            this.i = i;
        }

        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("load");
            printer.printLocal(i);
        }

        @Override
        public void execute(ExecutionContext context) { context.doLoad(i); }
    }

    class Store0 implements Instruction {
        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("store_0");
            printer.printLocal(0);
        }

        @Override
        public void execute(ExecutionContext context) { context.doStore(0); }
    }

    class Store1 implements Instruction {
        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("store_1");
            printer.printLocal(1);
        }

        @Override
        public void execute(ExecutionContext context) { context.doStore(1); }
    }

    class Store2 implements Instruction {
        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("store_2");
            printer.printLocal(2);
        }

        @Override
        public void execute(ExecutionContext context) { context.doStore(2); }
    }

    class Store implements Instruction {
        private final int i; // Local ID

        public Store(int i) {
            this.i = i;
        }

        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("store");
            printer.printLocal(i);
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
        public int stackAdjustment() { return 0; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("inc");
            printer.printLocal(i);
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
        public int stackAdjustment() { return 0; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("inc");
            printer.printLocal(i);
        }

        @Override
        public void execute(ExecutionContext context) { context.doDec(i); }
    }

    class ArrayLoad implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("aload");
        }

        @Override
        public void execute(ExecutionContext context) { context.doArrayLoad(); }
    }

    class ArrayStore implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 - 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("astore");
        }

        @Override
        public void execute(ExecutionContext context) { context.doArrayStore(); }
    }

    class ArrayInc implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("ainc");
        }

        @Override
        public void execute(ExecutionContext context) { context.doArrayInc(); }
    }

    class ArrayDec implements Instruction {
        @Override
        public int stackAdjustment() { return -1 - 1 + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("adec");
        }

        @Override
        public void execute(ExecutionContext context) { context.doArrayDec(); }
    }

    class NewList implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("new_list");
        }

        @Override
        public void execute(ExecutionContext context) { context.doNewList(); }
    }

    class NewMap implements Instruction {
        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("new_map");
        }

        @Override
        public void execute(ExecutionContext context) { context.doNewMap(); }
    }

    class Goto extends JumpInstruction {
        public Goto(int thenCp) {
            super(thenCp);
        }

        @Override
        public int stackAdjustment() { return 0; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("goto");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new Goto(nextCp); }

        @Override
        public void execute(ExecutionContext context) { context.setNextCp(getNextCp()); }
    }

    class IfEq extends JumpInstruction {
        public IfEq(int thenCp) {
            super(thenCp);
        }

        @Override
        public int stackAdjustment() { return -1 - 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("ifeq");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfNe(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfEq(nextCp); }

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
        public int stackAdjustment() { return -1 - 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("ifne");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfEq(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfNe(nextCp); }

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
        public int stackAdjustment() { return -1 - 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("ifgt");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfLe(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfGt(nextCp); }

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
        public int stackAdjustment() { return -1 - 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("ifle");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfGt(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfLe(nextCp); }

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
        public int stackAdjustment() { return -1 - 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("iflt");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfGe(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfLt(nextCp); }

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
        public int stackAdjustment() { return -1 - 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("ifge");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfLt(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfGe(nextCp); }

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
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("ifnz");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfZ(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfNz(nextCp); }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfTrue(getNextCp());
        }
    }

    class IfZ extends JumpInstruction {
        public IfZ(int thenCp) {
            super(thenCp);
        }

        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("ifz");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfNz(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfZ(nextCp); }

        @Override
        public void execute(ExecutionContext context) {
            context.doJumpIfFalse(getNextCp());
        }
    }

    class IfNull extends JumpInstruction {
        public IfNull(int thenCp) {
            super(thenCp);
        }

        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("if_null");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfNonNull(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfNull(nextCp); }

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
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("if_nonnull");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfNull(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfNonNull(nextCp); }

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
        public int stackAdjustment() { return -1 - 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("if_present");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfAbsent(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfPresent(nextCp); }

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
        public int stackAdjustment() { return -1 - 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("if_absent");
            printer.printCp(getNextCp());
        }

        @Override
        public JumpInstruction negated() { return new IfPresent(getNextCp()); }

        @Override
        public JumpInstruction withNextCp(int nextCp) { return new IfAbsent(nextCp); }

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
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("linear_switch");
            printer.beginSwitch();
            int[][] groupedLabels = new int[labels.length][];
            int[] groupedCps = new int[cps.length];
            int i = 0;
            if (labels.length > 0) {
                for (int c = cps[0], l = 0, j = 1; j < cps.length; j++) {
                    if (cps[j] != c) {
                        groupedLabels[i] = Arrays.copyOfRange(labels, l, j);
                        groupedCps[i] = c;
                        i++;
                        l = j;
                        c = cps[j];
                    }
                }
            }
            for (int j = 0; j < i; j++) {
                printer.printCase(groupedLabels[j], groupedCps[j]);
            }
            printer.printCase(null, defaultCp);
            printer.endSwitch();
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
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("binary_switch");
            printer.beginSwitch();
            int[][] groupedLabels = new int[labels.length][];
            int[] groupedCps = new int[cps.length];
            int i = 0;
            if (labels.length > 0) {
                for (int c = cps[0], l = 0, j = 1; j < cps.length; j++) {
                    if (cps[j] != c) {
                        groupedLabels[i] = Arrays.copyOfRange(labels, l, j);
                        groupedCps[i] = c;
                        i++;
                        l = j;
                        c = cps[j];
                    }
                }
            }
            for (int j = 0; j < i; j++) {
                printer.printCase(groupedLabels[j], groupedCps[j]);
            }
            printer.printCase(null, defaultCp);
            printer.endSwitch();
        }

        @Override
        public void execute(ExecutionContext context) { context.doBinarySwitch(labels, cps, defaultCp); }

        public void sort(ConstantPool cp) {
            qsort2(cp, labels, cps, 0, labels.length - 1);
        }

        private static void qsort2(ConstantPool cp, int[] keys, int[] values, int lo, int hi) {
            int i = lo;
            int j = hi;
            int pivot = keys[(i+j)/2];
            Address tmp1 = cp.getAddressEntry(pivot);
            Address tmp2;
            do {
                tmp2 = cp.getAddressEntry(keys[i]);
                while (tmp1.compareTo(tmp2) > 0) {
                    int index = keys[++i];
                    tmp2 = cp.getAddressEntry(index);
                }
                tmp2 = cp.getAddressEntry(keys[j]);
                while (tmp1.compareTo(tmp2) < 0) {
                    int index = keys[--j];
                    tmp2 = cp.getAddressEntry(index);
                }

                if (i <= j) {
                    int temp1 = keys[i];
                    keys[i] = keys[j];
                    keys[j] = temp1;
                    int temp2 = values[i];
                    values[i] = values[j];
                    values[j] = temp2;
                    i++;
                    j--;
                }
            } while (i <= j);
            if (lo < j) qsort2(cp, keys, values, lo, j);
            if (i < hi) qsort2(cp, keys, values, i, hi);
        }
    }

    class Call implements Instruction {
        private final int calleeId;
        private final int argCount;

        public Call(int calleeId, int argCount) {
            this.calleeId = calleeId;
            this.argCount = argCount;
        }

        @Override
        public int stackAdjustment() { return -argCount + 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("call");
            printer.printFuncRef(calleeId);
            printer.print(argCount);
        }

        @Override
        public void execute(ExecutionContext context) { context.doCall(calleeId, argCount); }
    }

    class GetConst implements Instruction {
        private final int constantId;

        public GetConst(int constantId) {
            this.constantId = constantId;
        }

        @Override
        public int stackAdjustment() { return 1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("get_const");
            printer.printConstRef(constantId);
        }

        @Override
        public void execute(ExecutionContext context) { context.doGetConst(constantId); }
    }
    class Return implements Instruction {
        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("return");
        }

        @Override
        public void execute(ExecutionContext context) { context.doReturn(); }
    }

    class Leave implements Instruction {
        @Override
        public int stackAdjustment() { return -1; }

        @Override
        public void print(InstructionPrinter printer) {
            printer.printName("leave");
        }

        @Override
        public void execute(ExecutionContext context) { context.doLeave(); }
    }
}
