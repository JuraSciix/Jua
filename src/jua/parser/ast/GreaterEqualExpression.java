package jua.parser.ast;

public class GreaterEqualExpression extends BinaryExpression {

    public GreaterEqualExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitGreaterEqual(this);
    }
}
