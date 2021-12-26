package jua.parser.ast;

public abstract class IncreaseExpression extends UnaryExpression {

    protected IncreaseExpression(Position position, Expression hs) {
        super(position, hs);
    }
}
