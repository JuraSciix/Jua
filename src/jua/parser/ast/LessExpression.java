package jua.parser.ast;

public class LessExpression extends BinaryExpression {

    public LessExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
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
