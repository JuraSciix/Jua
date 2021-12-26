package jua.parser.tree;

public class EqualExpression extends ConditionalExpression {

    public EqualExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.EQ, position, lhs, rhs);
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
