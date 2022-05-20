package jua.compiler;

import jua.compiler.Tree.*;
import jua.runtime.heap.DoubleOperand;
import jua.runtime.heap.LongOperand;
import jua.runtime.heap.Operand;
import jua.runtime.heap.StringOperand;

public final class TreeInfo {

    /**
     * @deprecated the {@link Lower} class removes the parens from AST, so this method is useless.
     */
    @Deprecated
    public static Expression removeParens(Expression tree) {
        if (tree == null)
            return null;

        Expression current = tree;

        while (current.isTag(Tag.PARENS)) {
            current = ((ParensExpression) current).expr;
        }

        return current;
    }

    public static boolean testShort(Expression expression) {
        if (expression instanceof IntExpression) {
            long value = ((IntExpression) expression).value;
            return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
        }
        return false;
    }

    public static int resolveLiteral(Code code, LiteralExpression expression) {
        if (expression instanceof IntExpression) {
            return code.resolveLong(((IntExpression) expression).value);
        }
        if (expression instanceof FloatExpression) {
            return code.resolveDouble(((FloatExpression) expression).value);
        }
        if (expression instanceof StringExpression) {
            return code.resolveString(((StringExpression) expression).value);
        }
        
        throw new AssertionError();
    }

    public static boolean isNull(Expression expression) {
        Expression expr = removeParens(expression);
        return expr == null || expr instanceof NullExpression;
    }

    public static Operand resolveLiteral(LiteralExpression expression) {
        if (expression instanceof IntExpression) {
            return LongOperand.valueOf(((IntExpression) expression).value);
        }
        if (expression instanceof FloatExpression) {
            return DoubleOperand.valueOf(((FloatExpression) expression).value);
        }
        if (expression instanceof StringExpression) {
            return StringOperand.valueOf(((StringExpression) expression).value);
        }
        throw new AssertionError();
    }

    private TreeInfo() {
        throw new UnsupportedOperationException();
    }
}
