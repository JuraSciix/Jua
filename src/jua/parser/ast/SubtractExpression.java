package jua.parser.ast;

public class SubtractExpression extends BinaryExpression {

    public SubtractExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitSubtract(this);
    }
}
