package jua.parser.tree;

public class GreaterExpression extends BinaryExpression {

    public GreaterExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.GT, position, lhs, rhs);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitGreater(this);
    }
}
