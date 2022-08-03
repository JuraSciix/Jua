package jua.compiler;

import jua.compiler.Tree.Expression;
import jua.compiler.Tree.Parens;
import jua.compiler.Tree.Tag;

public final class TreeInfo {

    public static Expression removeParens(Expression tree) {
        Expression result = tree;

        while (result != null && result.getTag() == Tag.PARENS) {
            Parens parens = (Parens) result;
            result = parens.expr;
        }

        return result;
    }

    public static Tag tagWithoutAsg(Tag tag) {
        switch (tag) {
            case ASG_ADD: return Tag.ADD;
            case ASG_SUB: return Tag.SUB;
            case ASG_MUL: return Tag.MUL;
            case ASG_DIV: return Tag.DIV;
            case ASG_REM: return Tag.REM;
            case ASG_SL: return Tag.SL;
            case ASG_SR: return Tag.SR;
            case ASG_AND: return Tag.AND;
            case ASG_OR: return Tag.OR;
            case ASG_XOR: return Tag.XOR;
            default: throw new IllegalArgumentException();
        }
    }

    public static boolean isConditionalTag(Tag tag) {
        return (tag.compareTo(Tag.FLOW_OR) | Tag.LE.compareTo(tag)) >= 0;
    }

    private TreeInfo() { throw new UnsupportedOperationException(); }
}
