package jua.parser.ast;

public class RemainderExpression extends BinaryExpression {

    public RemainderExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitRemainder(this);
    }
}
