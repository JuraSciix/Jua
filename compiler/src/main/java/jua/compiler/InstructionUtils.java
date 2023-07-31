package jua.compiler;

import jua.compiler.Tree.Tag;
import jua.interpreter.instruction.*;
import jua.interpreter.instruction.InstructionImpls.*;

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

    public static Instruction arrayIncreaseFromTag(Tag tag) {
        switch (tag) {
            case POSTINC: case PREINC: return ainc;
            case POSTDEC: case PREDEC: return adec;
            default: throw new AssertionError(tag);
        }
    }


    public static Instruction increaseFromTag(Tag tag, int index) {
        switch (tag) {
            case POSTINC: case PREINC: return new Inc(index);
            case POSTDEC: case PREDEC: return new Dec(index);
            default: throw new AssertionError(tag);
        }
    }

    public static Instruction fromBinaryAsgOpTag(Tag tag) {
        return fromBinaryOpTag(TreeInfo.stripAsgTag(tag));
    }

    public static JumpInstruction fromComparisonOpTag(Tag tag) {
        switch (tag) {
            case EQ: return new IfEq(0);
            case NE: return new IfNe(0);
            case GT: return new IfGt(0);
            case GE: return new IfGe(0);
            case LT: return new IfLt(0);
            case LE: return new IfLe(0);
            default: throw new AssertionError(tag);
        }
    }

    private InstructionUtils() {} // A utility class
}
