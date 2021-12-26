package jua.parser.ast;

public class EqualExpression extends BinaryExpression {

    public EqualExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitEqual(this);
    }
}
