package jua.compiler;

import jua.compiler.Code.ChainInstructionFactory;
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

    private InstructionUtils() {} // A utility class
}
