package jua.parser.tree;

public class OrExpression extends BinaryExpression {

    public OrExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.LOGOR, position, lhs, rhs);
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
