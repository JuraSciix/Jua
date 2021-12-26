package jua.parser.tree;

public class SubtractExpression extends BinaryExpression {

    public SubtractExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.SUB, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitSubtract(this);
    }
}
