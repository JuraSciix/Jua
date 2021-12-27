package jua.compiler;

import jua.parser.Tree;

public final class TreeInfo {

    public static Tree.Expression removeParens(Tree.Expression expression) {
        Tree.Expression current = expression;

        while (current instanceof Tree.ParensExpression) {
            current = ((Tree.ParensExpression) current).expr;
        }

        return current;
    }

    public static int line(Tree.Statement statement) {
        return statement.getPosition().line;
    }

    private TreeInfo() {
        throw new UnsupportedOperationException();
    }
}
