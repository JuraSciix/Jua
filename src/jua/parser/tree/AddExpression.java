package jua.parser.tree;

public class AddExpression extends BinaryExpression {

    public AddExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.ADD, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAdd(this);
    }
}
