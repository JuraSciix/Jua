package jua.parser.ast;

public class AndExpression extends BinaryExpression {

    public AndExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAnd(this);
    }
}
