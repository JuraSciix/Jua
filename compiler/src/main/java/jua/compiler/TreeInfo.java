package jua.compiler;

import jua.compiler.Tree.*;
import jua.compiler.Types.ListType;
import jua.compiler.Types.Type;
import jua.utils.Assert;

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
                for (Expression entry : listTree.entries) {
                    if (!isLiteral(entry)) {
                        return false;
                    }
                }
                return true;
            case MAPLIT:
                MapLiteral mapTree = (MapLiteral) innerTree;
                for (MapLiteral.Entry entry : mapTree.entries) {
                    if (!isLiteral(entry.key) || !isLiteral(entry.value)) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    public static Type literalType(Expression tree) {
        Expression innerTree = stripParens(tree);
        switch (innerTree.getTag()) {
            case LITERAL:
                Literal literalTree = (Literal) innerTree;
                return literalTree.type;
            case LISTLIT:
                ListLiteral listTree = (ListLiteral) innerTree;
                return new ListType(listTree.entries.map(TreeInfo::literalType).toArray(Type[]::new));
            case MAPLIT:
                MapLiteral mapTree = (MapLiteral) innerTree;
                return new Types.MapType(
                        mapTree.entries.map(e -> literalType(e.key)).toArray(Type[]::new),
                        mapTree.entries.map(e -> literalType(e.value)).toArray(Type[]::new)
                );
            default:
                throw new AssertionError(innerTree.getTag());
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
        Assert.ensure(innerTree.hasTag(Tag.LITERAL));
        Literal literalTree = (Literal) innerTree;
        Assert.ensure(literalTree.type.isLong());
        long longVal = literalTree.type.longValue();
        Assert.ensure((longVal >>> 16) == 0);
        return (short) longVal;
    }

    public static boolean isLiteralTrue(Expression tree) {
        Expression innerTree = stripParens(tree);
        if (innerTree.hasTag(Tag.LITERAL)) {
            Literal literalTree = (Literal) innerTree;
            return literalTree.type.booleanValue();
        }
        if (innerTree.hasTag(Tag.MAPLIT)) {
            MapLiteral arrayTree = (MapLiteral) innerTree;
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
        if (innerTree.hasTag(Tag.MAPLIT)) {
            MapLiteral arrayTree = (MapLiteral) innerTree;
            return arrayTree.entries.isEmpty();
        }
        return false;
    }

    /** Возвращает приоритет оператора. */
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
}
