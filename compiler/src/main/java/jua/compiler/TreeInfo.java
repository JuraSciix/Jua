package jua.compiler;

import jua.compiler.Tokens.TokenType;
import jua.compiler.Tree.*;
import jua.compiler.utils.Flow;

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
            case ASG_BIT_AND: return Tag.BIT_AND;
            case ASG_BIT_OR:  return Tag.BIT_OR;
            case ASG_BIT_XOR: return Tag.BIT_XOR;
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

    public static boolean isAccessible(Expression tree) {
        switch (stripParens(tree).getTag()) {
            case VAR:
            case ARRACC:
            case MEMACCESS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isLiteral(Expression tree) {
        Expression innerTree = stripParens(tree);
        switch (innerTree.getTag()) {
            case LITERAL:
                return true;
            case LISTLIT:
                ListLiteral listTree = (ListLiteral) innerTree;
                return Flow.allMatch(listTree.entries, TreeInfo::isLiteral);
            default:
                return false;
        }
    }

    public static Object literalType(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return literalTree.value;
        }
        throw new AssertionError(innerTree.getTag());
    }

    public static boolean isNull(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return literalTree.value == null;
        }
        return false;
    }

    public static boolean isLiteralShort(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            if (literalTree.value instanceof Long) {
                long longVal = (long) literalTree.value;
                return (longVal >>> 16) == 0;
            }
        }
        return false;
    }

    public static boolean isLiteralTrue(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return SemanticInfo.ofBoolean(literalTree.value).isTrue();
        }
        return false;
    }

    public static boolean isLiteralFalse(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return literalTree.value == null;
        }
        return false;
    }

    /**
     * Возвращает приоритет оператора.
     */
    public static int getOperatorPrecedence(Tag tag) {
        switch (tag) {
            case ASSIGN: case ASG_ADD: case ASG_SUB:
            case ASG_MUL: case ASG_DIV: case ASG_REM:
            case ASG_SL: case ASG_SR: case ASG_BIT_AND:
            case ASG_BIT_OR: case ASG_BIT_XOR:
                return 1;
            case COALESCE:
                return 2;
            case TERNARY:
                return 3;
            case OR:
                return 4;
            case AND:
                return 5;
            case BIT_OR:
                return 6;
            case BIT_XOR:
                return 7;
            case BIT_AND:
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
            case BIT_INV: case PREINC: case PREDEC:
                return 14;
            case POSTINC: case POSTDEC:
                return 15;
            case INVOCATION:
                return 16;
            case MEMACCESS: case ARRACC:
                return 17;
            case VAR:
                return 18;
            default:
                throw new AssertionError(tag);
        }
    }

    public static Tag getAsgTag(TokenType type) {
        switch (type) {
            case AMPEQ: return Tag.ASG_BIT_AND;
            case BAREQ: return Tag.ASG_BIT_OR;
            case CARETEQ: return Tag.ASG_BIT_XOR;
            case GTGTEQ: return Tag.ASG_SL;
            case LTLTEQ: return Tag.ASG_SR;
            case MINUSEQ: return Tag.ASG_SUB;
            case PERCENTEQ: return Tag.ASG_REM;
            case PLUSEQ: return Tag.ASG_ADD;
            case QUESQUESEQ: return Tag.ASG_COALESCE;
            case SLASHEQ: return Tag.ASG_DIV;
            case STAREQ: return Tag.ASG_MUL;
            default: throw new IllegalArgumentException(type.name());
        }
    }
}
