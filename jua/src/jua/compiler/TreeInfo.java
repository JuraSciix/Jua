package jua.compiler;

import jua.compiler.Tree.*;
import jua.runtime.heap.*;

public final class TreeInfo {

    /**
     * @deprecated the {@link CFold} class removes the parens from AST, so this method is useless.
     */
    @Deprecated
    public static Expression removeParens(Expression tree) {
        if (tree == null)
            return null;

        Expression current = tree;

        while (current.hasTag(Tag.PARENS)) {
            current = ((Parens) current).expr;
        }

        return current;
    }

    public static boolean testShort(Expression tree) {
        return isShortIntegerLiteral(tree);
    }

    public static boolean isShortIntegerLiteral(Expression tree) {
        if (tree == null) return false;
        if (!tree.hasTag(Tag.LITERAL)) return false;
        Literal literal = (Literal) tree;
        if (!(literal.value instanceof Number)) return false;
        long value = ((Number) literal.value).longValue();
        return (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE);
    }

    public static int resolveLiteral(Code code, Literal expression) {
        Object value = expression.value;
        if (value instanceof Long || value instanceof Integer) return code.resolveLong(((Number) value).longValue());
        if (value instanceof Double || value instanceof Float)
            return code.resolveDouble(((Number) value).doubleValue());
        if (value instanceof String) return code.resolveString((String) value);
        throw new IllegalArgumentException();
    }

    public static boolean isNull(Expression expression) {
        Expression expr = removeParens(expression);
        return expr == null || (expr instanceof Literal && ((Literal) expr).isNull());
    }

    public static boolean isCondition(Expression expression) {
        switch (expression.getTag()) {
            case LOGAND:
            case LOGOR:
            case LT:
            case LE:
            case GT:
            case NEQ:
            case GE:
            case EQ:
                return true;
            default: return false;
        }
    }

    public static Operand resolveLiteral(Literal expression) {
        Object value = expression.value;
        if (value instanceof Long || value instanceof Integer) return LongOperand.valueOf(((Number) value).longValue());
        if (value instanceof Double || value instanceof Float)
            return DoubleOperand.valueOf(((Number) value).doubleValue());
        if (value instanceof String) return StringOperand.valueOf((String) value);
        if (value instanceof Boolean) return BooleanOperand.valueOf(((Boolean) value));
        assert value == null;
        return NullOperand.NULL;
    }

    private TreeInfo() {
        throw new UnsupportedOperationException();
    }
}
