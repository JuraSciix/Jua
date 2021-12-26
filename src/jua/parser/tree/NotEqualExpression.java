package jua.parser.tree;

public class NotEqualExpression extends BinaryExpression {

    public NotEqualExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.NEQ, position, lhs, rhs);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNotEqual(this);
    }
}
