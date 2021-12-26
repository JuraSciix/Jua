package jua.parser.tree;

public class MultiplyExpression extends BinaryExpression {

    public MultiplyExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.MUL, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitMultiply(this);
    }
}
