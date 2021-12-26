package jua.parser.tree;

public class AndExpression extends BinaryExpression {

    public AndExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.LOGAND, position, lhs, rhs);
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
