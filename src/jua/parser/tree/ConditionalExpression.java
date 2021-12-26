package jua.parser.tree;

public abstract class ConditionalExpression extends BinaryExpression {

    protected ConditionalExpression(Tag tag, Position position, Expression lhs, Expression rhs) {
        super(tag, position, lhs, rhs);
    }
}
