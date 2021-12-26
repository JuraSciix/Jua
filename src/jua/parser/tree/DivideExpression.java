package jua.parser.tree;

public class DivideExpression extends BinaryExpression {

    public DivideExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.DIV, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitDivide(this);
    }
}
