package jua.parser.ast;

public class LessEqualExpression extends BinaryExpression {

    public LessEqualExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
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
