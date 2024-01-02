package jua.compiler;

import jua.compiler.Tokens.TokenType;
import jua.compiler.Tree.*;
import jua.compiler.utils.Flow;

public final class TreeInfo {

    public static Expr stripParens(Expr tree) {
        Expr e = tree;
        while (e.hasTag(Tag.PARENS)) {
            Parens p = (Parens) e;
            e = p.expr;
        }
        return e;
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

    public static boolean isAccessible(Expr tree) {
        switch (stripParens(tree).getTag()) {
            case VAR:
            case ARRACC:
            case MEMACCESS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isLiteral(Expr tree) {
        Expr innerTree = stripParens(tree);
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

    public static Object literalType(Expr tree) {
        Expr innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return literalTree.value;
        }
        throw new AssertionError(innerTree.getTag());
    }

    public static boolean isNull(Expr tree) {
        Expr innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return literalTree.value == null;
        }
        return false;
    }

    public static boolean isLiteralShort(Expr tree) {
        Expr innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            if (literalTree.value instanceof Long) {
                long longVal = (long) literalTree.value;
                return (longVal >>> 16) == 0;
            }
        }
        return false;
    }

    public static boolean isLiteralTrue(Expr tree) {
        Expr innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return SemanticInfo.ofBoolean(literalTree.value).isTrue();
        }
        return false;
    }

    public static boolean isLiteralFalse(Expr tree) {
        Expr innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return literalTree.value == null;
        }
        return false;
    }

    public static Tag getAsgTag(TokenType tokenType) {
        switch (tokenType) {
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
            default: throw new AssertionError(tokenType);
        }
    }

    public static Tag getBinOpTag(TokenType tokenType) {
        switch (tokenType) {
            case BARBAR: return Tag.OR;
            case AMPAMP: return Tag.AND;
            case BAR: return Tag.BIT_OR;
            case CARET: return Tag.BIT_XOR;
            case AMP: return Tag.BIT_AND;
            case EQ: return Tag.ASSIGN;
            case EQEQ: return Tag.EQ;
            case BANGEQ: return Tag.NE;
            case GT: return Tag.GT;
            case GTEQ: return Tag.GE;
            case LT: return Tag.LT;
            case LTEQ: return Tag.LE;
            case GTGT: return Tag.SL;
            case LTLT: return Tag.SR;
            case MINUS: return Tag.SUB;
            case PLUS: return Tag.ADD;
            case PERCENT: return Tag.REM;
            case SLASH: return Tag.DIV;
            case STAR: return Tag.MUL;
            case QUESQUES: return Tag.COALESCE;
            default: throw new AssertionError(tokenType);
        }
    }

    public static Tag getUnaryOpTag(TokenType tokenType) {
        switch (tokenType) {
            case BANG: return Tag.NOT;
            case MINUS: return Tag.NEG;
            case PLUS: return Tag.POS;
            case TILDE: return Tag.BIT_INV;
            case PLUSPLUS: return Tag.PREINC;
            case MINUSMINUS: return Tag.PREDEC;
            default: throw new AssertionError(tokenType);
        }
    }

    public static int getBinOpPrecedence(Tag operatorTag) {
        switch (operatorTag) {
            case BIT_OR: return 10;
            case BIT_AND: return 20;
            case OR: return 110;
            case BIT_XOR: return 120;
            case AND: return 130;
            case EQ: case NE: return 200;
            case GT: case GE: case LT: case LE: return 300;
            case SL: case SR: return 500;
            case SUB: case ADD: return 700;
            case REM: case DIV: case MUL: return 800;
            case COALESCE: return 1000;
            default: throw new AssertionError(operatorTag);
        }
    }
}
