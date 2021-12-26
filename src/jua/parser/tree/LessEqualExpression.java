package jua.parser.tree;

public class LessEqualExpression extends ConditionalExpression {

    public LessEqualExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.LE, position, lhs, rhs);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitLessEqual(this);
    }
}
