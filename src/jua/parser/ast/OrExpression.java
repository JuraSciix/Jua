package jua.parser.ast;

public class OrExpression extends BinaryExpression {

    public OrExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitOr(this);
    }
}
