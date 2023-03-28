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
//            case POSTINC: case PREINC: return ainc;
//            case POSTDEC: case PREDEC: return adec;
            default: throw new AssertionError(tag);
        }
    }

    public static Instruction fromBinaryAsgOpTag(Tag tag) {
        return fromBinaryOpTag(TreeInfo.stripAsgTag(tag));
    }

    public static JumpInstruction fromComparisonOpTag(Tag tag) {
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
