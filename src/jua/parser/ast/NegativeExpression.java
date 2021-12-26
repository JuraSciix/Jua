package jua.parser.ast;

public class NegativeExpression extends UnaryExpression {

    public NegativeExpression(Position position, Expression hs) {
        super(position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNegative(this);
    }
}
