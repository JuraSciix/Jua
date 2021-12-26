package jua.parser.ast;

public class LeftShiftExpression extends BinaryExpression {

    public LeftShiftExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitLeftShift(this);
    }
}
