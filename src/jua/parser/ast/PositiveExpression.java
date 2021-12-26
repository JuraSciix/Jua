package jua.parser.ast;

public class PositiveExpression extends UnaryExpression {

    public PositiveExpression(Position position, Expression hs) {
        super(position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPositive(this);
    }
}
