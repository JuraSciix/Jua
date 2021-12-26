package jua.parser.tree;

public class ShiftLeftExpression extends BinaryExpression {

    public ShiftLeftExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.SL, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitLeftShift(this);
    }
}
