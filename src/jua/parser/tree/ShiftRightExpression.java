package jua.parser.tree;

public class ShiftRightExpression extends BinaryExpression {

    public ShiftRightExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.SR, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitRightShift(this);
    }
}
