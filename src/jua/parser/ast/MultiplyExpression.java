package jua.parser.ast;

public class MultiplyExpression extends BinaryExpression {

    public MultiplyExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitMultiply(this);
    }
}
