package jua.parser.tree;

public abstract class IncreaseExpression extends UnaryExpression {

    protected IncreaseExpression(Tag tag, Position position, Expression hs) {
        super(tag, position, hs);
    }
}
