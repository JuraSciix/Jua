package jua.compiler;

import jua.compiler.Tree.Tag;

import static jua.compiler.CompHelper.stripAsgTag;

public class InstructionUtils {

    public static int fromBinaryOpTag(Tag tag) {
        switch (tag) {
            case ADD: return OPCodes.Add;
            case SUB: return OPCodes.Sub;
            case MUL: return OPCodes.Mul;
            case DIV: return OPCodes.Div;
            case REM: return OPCodes.Rem;
            case SL: return OPCodes.Shl;
            case SR: return OPCodes.Shr;
            case BIT_AND: return OPCodes.And;
            case BIT_OR: return OPCodes.Or;
            case BIT_XOR: return OPCodes.Xor;
            default: throw new AssertionError(tag);
        }
    }

    public static int fromUnaryOpTag(Tag tag) {
        switch (tag) {
            case NEG: return OPCodes.Neg;
            case POS: return OPCodes.Pos;
            case BIT_INV: return OPCodes.Not;
//            case POSTINC: case PREINC: return ainc;
//            case POSTDEC: case PREDEC: return adec;
            default: throw new AssertionError(tag);
        }
    }

    public static int arrayIncreaseFromTag(Tag tag) {
        switch (tag) {
            case POSTINC: case PREINC: return OPCodes.ArrayInc;
            case POSTDEC: case PREDEC: return OPCodes.ArrayDec;
            default:
                throw new AssertionError(tag);
        }
    }

    public static int increaseFromTag(Tag tag) {
        switch (tag) {
            case POSTINC: case PREINC: return OPCodes.Inc;
            case POSTDEC: case PREDEC: return OPCodes.Dec;
            default: throw new AssertionError(tag);
        }
    }

    public static int fromBinaryAsgOpTag(Tag tag) {
        return fromBinaryOpTag(stripAsgTag(tag));
    }

    public static int fromComparisonOpTag(Tag tag) {
        switch (tag) {
            case EQ: return OPCodes.IfEq;
            case NE: return OPCodes.IfNe;
            case GT: return OPCodes.IfGt;
            case GE: return OPCodes.IfGe;
            case LT: return OPCodes.IfLt;
            case LE: return OPCodes.IfLe;
            default: throw new AssertionError(tag);
        }
    }

    private InstructionUtils() {} // A utility class

    public interface OPCodes {
        int
                Nop = 0,
                ConstNull = 1,
                ConstTrue = 2,
                ConstFalse = 3,
                ConstIntM1 = 4,
                ConstInt0 = 5,
                ConstInt1 = 6,
                ConstInt2 = 7,
                Dup = 8,
                DupX1 = 9,
                DupX2 = 10,
                Swap = 11,
                Dup2 = 12,
                Dup2X1 = 13,
                Dup2X2 = 14,
                Push = 15,
                NewList = 16,
                NewMap = 17,
                Pop = 18,
                Pop2 = 19,
                Add = 20,
                Sub = 21,
                Mul = 22,
                Div = 23,
                Rem = 24,
                And = 25,
                Or = 26,
                Xor = 27,
                Shl = 28,
                Shr = 29,
                Length = 30,
                Pos = 31,
                Neg = 32,
                Not = 33,
                Load = 34,
                Load0 = 35,
                Load1 = 36,
                Load2 = 37,
                Store = 38,
                Store0 = 39,
                Store1 = 40,
                Store2 = 41,
                Inc = 42,
                Dec = 43,
                ArrayLoad = 44,
                ArrayStore = 45,
                ArrayDec = 46,
                ArrayInc = 47,
                Goto = 48,
                IfEq = 49,
                IfNe = 50,
                IfGe = 51,
                IfLt = 52,
                IfGt = 53,
                IfLe = 54,
                IfZ = 55,
                IfNz = 56,
                IfNull = 57,
                IfNonNull = 58,
                IfPresent = 59,
                IfAbsent = 60,
                LinearSwitch = 61,
                BinarySwitch = 62,
                Call = 63,
                Return = 64,
                Leave = 65,

                ShLoad = 66, // Shared Load. Чтение разделенной переменной
                ShStore = 67, // Shared Store. Запись в разделенную переменную

        _InstrCount = Leave + 1;
    }

    private static final OpData[] OP_DATA = new OpData[OPCodes._InstrCount];

    static class OpData {
        public final String name;
        public final int stackAdj;

        public OpData(String name, int stackAdj) {
            this.name = name;
            this.stackAdj = stackAdj;
        }
    }

    private static void opData(int opcode, String name, int stackAdj) {
        OP_DATA[opcode] = new OpData(name, stackAdj);
    }

    static {
        opData(OPCodes.Nop, "nop", 0);
        opData(OPCodes.ConstNull, "const_null", 1);
        opData(OPCodes.ConstTrue, "const_true", 1);
        opData(OPCodes.ConstFalse, "const_false", 1);
        opData(OPCodes.ConstInt0, "const_i0", 1);
        opData(OPCodes.ConstInt1, "const_i1", 1);
        opData(OPCodes.ConstInt2, "const_i2", 1);
        opData(OPCodes.ConstIntM1, "const_im1", 1);
        opData(OPCodes.Push, "push", 1);
        opData(OPCodes.NewList, "newlist", 0);
        opData(OPCodes.NewMap, "newmap", 1);
        opData(OPCodes.Dup, "dup", 1);
        opData(OPCodes.DupX1, "dup_x1", 1);
        opData(OPCodes.DupX2, "dup_x2", 1);
        opData(OPCodes.Dup2, "dup2", 2);
        opData(OPCodes.Dup2X1, "dup2_x1", 2);
        opData(OPCodes.Dup2X2, "dup2_x2", 2);
        opData(OPCodes.Pop, "pop", -1);
        opData(OPCodes.Pop2, "pop2", -2);
        opData(OPCodes.Add, "add", -1);
        opData(OPCodes.Sub, "sub", -1);
        opData(OPCodes.Mul, "mul", -1);
        opData(OPCodes.Div, "div", -1);
        opData(OPCodes.Rem, "rem", -1);
        opData(OPCodes.And, "and", -1);
        opData(OPCodes.Or, "or", -1);
        opData(OPCodes.Xor, "xor", -1);
        opData(OPCodes.Shl, "shl", -1);
        opData(OPCodes.Shr, "shr", -1);
        opData(OPCodes.IfEq, "ifeq", -2);
        opData(OPCodes.IfNe, "ifne", -2);
        opData(OPCodes.IfGe, "ifge", -2);
        opData(OPCodes.IfLt, "iflt", -2);
        opData(OPCodes.IfGt, "ifgt", -2);
        opData(OPCodes.IfLe, "ifle", -2);
        opData(OPCodes.IfZ, "ifz", -1);
        opData(OPCodes.IfNz, "ifnz", -1);
        opData(OPCodes.IfNull, "if_null", -1);
        opData(OPCodes.IfNonNull, "if_nonnull", -1);
        opData(OPCodes.IfPresent, "if_present", -2);
        opData(OPCodes.IfAbsent, "if_absent", -2);
        opData(OPCodes.Goto, "goto", 0);
        opData(OPCodes.Length, "length", 0);
        opData(OPCodes.Load, "load", 1);
        opData(OPCodes.Load0, "load_0", 1);
        opData(OPCodes.Load1, "load_1", 1);
        opData(OPCodes.Load2, "load_2", 1);
        opData(OPCodes.Store, "store", -1);
        opData(OPCodes.Store0, "store_0", -1);
        opData(OPCodes.Store1, "store_1", -1);
        opData(OPCodes.Store2, "store_2", -1);
        opData(OPCodes.Inc, "inc", 0);
        opData(OPCodes.Dec, "dec", 0);
        opData(OPCodes.ArrayLoad, "aload", -1);
        opData(OPCodes.ArrayStore, "astore", -3);
        opData(OPCodes.ArrayDec, "adec", -1);
        opData(OPCodes.ArrayInc, "ainc", -1);
        opData(OPCodes.Pos, "pos", 0);
        opData(OPCodes.Neg, "neg", 0);
        opData(OPCodes.Not, "not", 0);
        opData(OPCodes.Call, "call", 0); // todo
        opData(OPCodes.LinearSwitch, "linearswitch", -1);
        opData(OPCodes.BinarySwitch, "binaryswitch", -1);
        opData(OPCodes.Return, "return", -1);
        opData(OPCodes.Leave, "leave", 0);

    }

    public static int getOpcodeStackAdj(int opcode) {
        return OP_DATA[opcode].stackAdj;
    }

    public static String getOpcodeName(int opcode) {
        if (-1 >= opcode || opcode >= OP_DATA.length) {
            return getRawOpcodeString(opcode);
        }
        assert OP_DATA[opcode] != null : "0x" + getRawOpcodeString(opcode);
        return OP_DATA[opcode].name;
    }

    public static String getRawOpcodeString(int opcode) {
        return "0x" + Integer.toHexString(opcode);
    }

    public static int negate(int opcode) {
        return ((opcode+1) ^ 1)-1;
    }

    public interface InstrVisitor {
        void visitJump(JumpInstrNode node);
        void visitSingle(SingleInstrNode node);
        void visitCall(CallInstrNode node);
        void visitIndexed(IndexedInstrNode node);
        void visitConst(ConstantInstrNode node);
        void visitSwitch(SwitchInstrNode node);
    }

    public abstract static class InstrNode {
        public final int opcode;

        public InstrNode(int opcode) {
            this.opcode = opcode;
        }

        public int stackAdjustment() {
            return InstructionUtils.getOpcodeStackAdj(opcode);
        }

        public abstract void accept(InstrVisitor visitor);

        public void setOffset(int offset) {
            throw new IllegalStateException(getClass().getName());
        }
    }

    public static class ConstantInstrNode extends InstrNode {
        public final int index;

        public ConstantInstrNode(int opcode, int index) {
            super(opcode);
            this.index = index;
        }

        @Override
        public void accept(InstrVisitor visitor) {
            visitor.visitConst(this);
        }
    }

    public static class IndexedInstrNode extends InstrNode {
        public final int index;

        public IndexedInstrNode(int opcode, int index) {
            super(opcode);
            this.index = index;
        }

        @Override
        public void accept(InstrVisitor visitor) {
            visitor.visitIndexed(this);
        }
    }

    public static class CallInstrNode extends InstrNode {
        public final int callee;
        public final int argc;

        public CallInstrNode(int opcode, int callee, int argc) {
            super(opcode);
            this.callee = callee;
            this.argc = argc;
        }

        @Override
        public int stackAdjustment() {
            return -argc + 1;
        }

        @Override
        public void accept(InstrVisitor visitor) {
            visitor.visitCall(this);
        }
    }

    public static class SingleInstrNode extends InstrNode {

        public SingleInstrNode(int opcode) {
            super(opcode);
            if (opcode == OPCodes.Push) {
                throw new RuntimeException();
            }
        }

        @Override
        public void accept(InstrVisitor visitor) {
            visitor.visitSingle(this);
        }
    }

    public static class JumpInstrNode extends InstrNode {
        public int offset;

        public JumpInstrNode(int opcode) {
            super(opcode);
        }

        @Override
        public void setOffset(int offset) {
            this.offset = offset;
        }

        @Override
        public void accept(InstrVisitor visitor) {
            visitor.visitJump(this);
        }
    }

    public static class SwitchInstrNode extends InstrNode {

        public int[] literals;
        public int[] dstIps;
        public int defCp;

        public SwitchInstrNode(int opcode) {
            super(opcode);
        }

        @Override
        public void accept(InstrVisitor visitor) {
            visitor.visitSwitch(this);
        }
    }

}
