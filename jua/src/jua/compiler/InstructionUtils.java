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
            case SL:  return Shl.INSTANCE;
            case SR:  return Shr.INSTANCE;
            case AND: return And.INSTANCE;
            case OR:  return Or.INSTANCE;
            case XOR: return Xor.INSTANCE;
            default:  throw new AssertionError(tag);
        }
    }

    public static Instruction fromUnaryOpTag(Tag tag) {
        switch (tag) {
            case NEG:     return Neg.INSTANCE;
            case POS:     return Pos.INSTANCE;
            case INVERSE: return Not.INSTANCE;
            case POSTINC: case PREINC: return Inc.INSTANCE;
            case POSTDEC: case PREDEC: return Dec.INSTANCE;
            default: throw new AssertionError(tag);
        }
    }

    public static Instruction fromBinaryAsgOpTag(Tag tag) {
        return fromBinaryOpTag(TreeInfo.stripAsgTag(tag));
    }

    public static JumpInstruction fromConstComparisonOpTag(Tag tag, short comparing) {
        switch (tag) {
            case EQ: return new ifconstne(comparing);
            case NE: return new ifconsteq(comparing);
            case GT: return new ifconstle(comparing);
            case GE: return new ifconstlt(comparing);
            case LT: return new ifconstge(comparing);
            case LE: return new ifconstgt(comparing);
            default: throw new AssertionError(tag);
        }
    }

    public static JumpInstruction fromComparisonOpTag(Tag tag) {
        switch (tag) {
            case EQ: return new Ifne();
            case NE: return new Ifeq();
            case GT: return new Ifle();
            case GE: return new Iflt();
            case LT: return new Ifge();
            case LE: return new Ifgt();
            default: throw new AssertionError(tag);
        }
    }

    private InstructionUtils() {} // A utility class
}
