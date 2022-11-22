package jua.compiler;

import jua.compiler.Tree.Tag;
import jua.interpreter.instruction.*;

public class InstructionUtils {

    public static Instruction fromBinaryOpTag(Tag tag) {
        switch (tag) {
            case ADD: return Add.INSTANCE;
            case SUB: return Sub.INSTANCE;
            case MUL: return Mul.INSTANCE;
            case DIV: return Div.INSTANCE;
            case REM: return Rem.INSTANCE;
            case SL: return Shl.INSTANCE;
            case SR: return Shr.INSTANCE;
            case AND: return And.INSTANCE;
            case OR: return Or.INSTANCE;
            case XOR: return Xor.INSTANCE;
            default: throw new IllegalArgumentException(tag.name());
        }
    }

    public static Instruction fromUnaryOpTag(Tag tag) {
        switch (tag) {
            case NEG: return Neg.INSTANCE;
            case POS: return Pos.INSTANCE;
            case INVERSE: return Not.INSTANCE;
            default: throw new IllegalArgumentException(tag.name());
        }
    }

    public static JumpInstruction fromConstComparisonOpTag(Tag tag, int comparing, boolean negate) {
        if (negate) {
            tag = TreeInfo.negateComparisonTag(tag);
        }
        switch (tag) {
            case EQ: return new Ifeq(comparing);
            case NE: return new Ifne(comparing);
            case GT: return new Ifgt(comparing);
            case GE: return new Ifge(comparing);
            case LT: return new Iflt(comparing);
            case LE: return new Ifle(comparing);
            default: throw new AssertionError(tag);
        }
    }

    public static JumpInstruction fromComparisonOpTag(Tag tag, boolean negate) {
        if (negate) {
            tag = TreeInfo.negateComparisonTag(tag);
        }
        switch (tag) {
            case EQ: return new Ifcmpeq();
            case NE: return new Ifcmpne();
            case GT: return new Ifcmpgt();
            case GE: return new Ifcmpge();
            case LT: return new Ifcmplt();
            case LE: return new Ifcmple();
            default: throw new AssertionError(tag);
        }
    }

    private InstructionUtils() {} // A utility class
}
