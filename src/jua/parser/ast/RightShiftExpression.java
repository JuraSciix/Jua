package jua.parser.ast;

public class RightShiftExpression extends BinaryExpression {

    public RightShiftExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitRightShift(this);
    }
}
