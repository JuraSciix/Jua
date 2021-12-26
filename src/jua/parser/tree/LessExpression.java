package jua.parser.tree;

public class LessExpression extends ConditionalExpression {

    public LessExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.LT, position, lhs, rhs);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitLess(this);
    }
}
