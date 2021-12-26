package jua.parser.ast;

public class AddExpression extends BinaryExpression {

    public AddExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAdd(this);
    }
}
