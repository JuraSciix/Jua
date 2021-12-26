package jua.parser.tree;

public class RemainderExpression extends BinaryExpression {

    public RemainderExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.REM, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitRemainder(this);
    }
}
