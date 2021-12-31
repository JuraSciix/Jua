package jua.compiler;

import jua.interpreter.lang.FloatOperand;
import jua.interpreter.lang.IntOperand;
import jua.interpreter.lang.Operand;
import jua.interpreter.lang.StringOperand;
import jua.parser.Tree;
import jua.parser.Tree.*;

public final class TreeInfo {

    public static Expression removeParens(Expression expression) {
        Tree.Expression current = expression;

        while (current instanceof Tree.ParensExpression) {
            current = ((Tree.ParensExpression) current).expr;
        }

        return current;
    }

    public static int line(Statement statement) {
        return statement.getPosition().line;
    }

    public static String sourceName(Statement statement) {
        return statement.getPosition().filename;
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
            return code.resolveConstant(((IntExpression) expression).value);
        }
        if (expression instanceof FloatExpression) {
            return code.resolveConstant(((FloatExpression) expression).value);
        }
        if (expression instanceof StringExpression) {
            return code.resolveConstant(((StringExpression) expression).value);
        }
        throw new AssertionError();
    }

    public static Operand resolveLiteral(LiteralExpression expression) {
        if (expression instanceof IntExpression) {
            return IntOperand.valueOf(((IntExpression) expression).value);
        }
        if (expression instanceof FloatExpression) {
            return FloatOperand.valueOf(((FloatExpression) expression).value);
        }
        if (expression instanceof StringExpression) {
            return StringOperand.valueOf(((StringExpression) expression).value);
        }
        throw new AssertionError();
    }

    private TreeInfo() { throw new UnsupportedOperationException(); }
}
