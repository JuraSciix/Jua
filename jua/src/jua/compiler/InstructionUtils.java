package jua.compiler;

import jua.compiler.Tree.Tag;
import jua.interpreter.instruction.*;

import static jua.compiler.TreeInfo.negateComparisonTag;
import static jua.compiler.TreeInfo.tagWithoutAsg;

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
            case POSTINC: case PREINC: return Inc.INSTANCE;
            case POSTDEC: case PREDEC: return Dec.INSTANCE;
            default: throw new IllegalArgumentException(tag.name());
        }
    }

    public static Instruction fromBinaryAsgOpTag(Tag tag) {
        return InstructionUtils.fromBinaryOpTag(tagWithoutAsg(tag));
    }

    public static JumpInstruction fromConstComparisonOpTag(Tag tag, int comparing, boolean negate) {
        if (!negate) {
            tag = negateComparisonTag(tag);
        }
        switch (tag) {
            case EQ: return new ifconsteq(comparing);
            case NE: return new ifconstne(comparing);
            case GT: return new ifconstgt(comparing);
            case GE: return new ifconstge(comparing);
            case LT: return new ifconstlt(comparing);
            case LE: return new ifconstle(comparing);
            default: throw new AssertionError(tag);
        }
    }

    public static JumpInstruction fromComparisonOpTag(Tag tag, boolean negate) {
        if (!negate) {
            tag = negateComparisonTag(tag);
        }
        switch (tag) {
            case EQ: return new Ifeq();
            case NE: return new Ifne();
            case GT: return new Ifgt();
            case GE: return new Ifge();
            case LT: return new Iflt();
            case LE: return new Ifle();
            default: throw new AssertionError(tag);
        }
    }

    private InstructionUtils() {} // A utility class
}
