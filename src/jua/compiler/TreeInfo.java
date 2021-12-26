package jua.compiler;

import jua.parser.tree.*;

public final class TreeInfo {

    public static Expression removeParens(Expression expression) {
        Expression current = expression;

        while (current instanceof ParensExpression) {
            current = ((ParensExpression) current).expr;
        }

        return current;
    }

    public static int line(Statement statement) {
        return statement.getPosition().line;
    }

    public static long getInteger(Expression expression) {
        return ((IntExpression) expression).value;
    }

    public static double getDouble(Expression expression) {
        return ((FloatExpression) expression).value;
    }

    public static String getString(Expression expression) {
        return ((StringExpression) expression).value;
    }

    private TreeInfo() {
        throw new UnsupportedOperationException();
    }
}
