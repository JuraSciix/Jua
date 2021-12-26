package jua.parser.tree;

public class GreaterEqualExpression extends ConditionalExpression {

    public GreaterEqualExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.GE, position, lhs, rhs);
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
