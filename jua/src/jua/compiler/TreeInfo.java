package jua.compiler;

import jua.compiler.Tree.Name;
import jua.compiler.Tree.Parens;
import jua.compiler.Tree.Tag;

public final class TreeInfo {

    public static Tree removeParens(Tree tree) {
        Tree result = tree;

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
            default: throw new IllegalArgumentException(tag.name());
        }
    }

    public static boolean isConditionalTag(Tag tag) {
        return (tag.compareTo(Tag.FLOW_OR) | Tag.LE.compareTo(tag)) >= 0;
    }

    public static boolean isNameEquals(Name name1, Name name2) {
        if (name1 == name2) return true;
        if (name1 == null || name2 == null) return false;
        if (name1.value == null || name2.value == null) return false;
        return name1.value.equals(name2.value);
    }

    public static boolean testNamedArgument(Target target, Name name1, Name name2) {
        if (!target.isNamedArgumentInvocationAllowed()) {
            return name1 == null && name2 == null;
        }
        return isNameEquals(name1, name2);
    }

    private TreeInfo() { throw new UnsupportedOperationException(); }
}
