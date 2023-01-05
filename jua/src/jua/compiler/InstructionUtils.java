package jua.compiler;

import jua.compiler.Tree.Tag;
import jua.interpreter.instruction.*;

import static jua.compiler.InstructionFactory.*;

public class InstructionUtils {

    public static Instruction fromBinaryOpTag(Tag tag) {
        switch (tag) {
            case ADD: return add;
            case SUB: return sub;
            case MUL: return mul;
            case DIV: return div;
            case REM: return rem;
            case SL:  return shl;
            case SR:  return shr;
            case BIT_AND: return and;
            case BIT_OR:  return or;
            case BIT_XOR: return xor;
            default:  throw new AssertionError(tag);
        }
    }

    public static Instruction fromUnaryOpTag(Tag tag) {
        switch (tag) {
            case NEG:     return neg;
            case POS:     return pos;
            case BIT_INV: return not;
            case POSTINC: case PREINC: return inc;
            case POSTDEC: case PREDEC: return dec;
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
