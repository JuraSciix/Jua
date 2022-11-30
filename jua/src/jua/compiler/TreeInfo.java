package jua.compiler;

import jua.compiler.Tree.*;
import jua.util.Assert;

public final class TreeInfo {

    public static Expression stripParens(Expression tree) {
        Expression resultTree = tree;
        while (resultTree.hasTag(Tag.PARENS)) {
            Parens parensTree = (Parens) resultTree;
            resultTree = parensTree.expr;
        }
        return resultTree;
    }

    public static Tag stripAsgTag(Tag tag) {
        switch (tag) {
            case ASG_ADD: return Tag.ADD;
            case ASG_SUB: return Tag.SUB;
            case ASG_MUL: return Tag.MUL;
            case ASG_DIV: return Tag.DIV;
            case ASG_REM: return Tag.REM;
            case ASG_SL:  return Tag.SL;
            case ASG_SR:  return Tag.SR;
            case ASG_AND: return Tag.AND;
            case ASG_OR:  return Tag.OR;
            case ASG_XOR: return Tag.XOR;
            default: throw new AssertionError(tag);
        }
    }

    public static Tag negateCmpTag(Tag tag) {
        switch (tag) {
            case EQ: return Tag.NE;
            case NE: return Tag.EQ;
            case GT: return Tag.LE;
            case GE: return Tag.LT;
            case LT: return Tag.GE;
            case LE: return Tag.GT;
            default: throw new AssertionError(tag);
        }
    }

    public static boolean isLiteralNull(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return literalTree.type.isNull();
        }
        return false;
    }

    public static boolean isLiteralShort(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            if (literalTree.type.isLong()) {
                long longVal = literalTree.type.longValue();
                return (longVal >>> 16) == 0;
            }
        }
        return false;
    }

    public static short getLiteralShort(Expression tree) {
        Expression innerTree = stripParens(tree);
        Assert.check(innerTree.hasTag(Tag.LITERAL));
        Literal literalTree = (Literal) innerTree;
        Assert.check(literalTree.type.isLong());
        long longVal = literalTree.type.longValue();
        Assert.check((longVal >>> 16) == 0);
        return (short) longVal;
    }

    public static boolean isLiteralTrue(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return literalTree.type.booleanValue();
        }
        if (innerTree.hasTag(Tag.ARRAYLITERAL)) {
            ArrayLiteral arrayTree = (ArrayLiteral) innerTree;
            return !arrayTree.entries.isEmpty();
        }
        return false;
    }

    public static boolean isLiteralFalse(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return !literalTree.type.booleanValue();
        }
        if (innerTree.hasTag(Tag.ARRAYLITERAL)) {
            ArrayLiteral arrayTree = (ArrayLiteral) innerTree;
            return arrayTree.entries.isEmpty();
        }
        return false;
    }

    /** Возвращает приоритет оператора. */
    public static int getOperatorPrecedence(Tag tag) {
        switch (tag) {
            case ASSIGN: case ASG_ADD: case ASG_SUB:
            case ASG_MUL: case ASG_DIV: case ASG_REM:
            case ASG_SL: case ASG_SR: case ASG_AND:
            case ASG_OR: case ASG_XOR:
                return 1;
            case NULLCOALESCE:
                return 2;
            case TERNARY:
                return 3;
            case FLOW_OR:
                return 4;
            case FLOW_AND:
                return 5;
            case OR:
                return 6;
            case XOR:
                return 7;
            case AND:
                return 8;
            case EQ: case NE:
                return 9;
            case GT: case GE: case LT: case LE:
                return 10;
            case SL: case SR:
                return 11;
            case ADD: case SUB:
                return 12;
            case MUL: case DIV: case REM:
                return 13;
            case POS: case NEG: case NOT:
            case INVERSE: case PREINC: case PREDEC:
                return 14;
            case POSTINC: case POSTDEC:
                return 15;
            case INVOCATION:
                return 16;
            case MEMACCESS: case ARRAYACCESS:
                return 17;
            case VARIABLE:
                return 18;
            default:
                throw new AssertionError(tag);
        }
    }
}
