package jua.parser.ast;

public class DivideExpression extends BinaryExpression {

    public DivideExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitDivide(this);
    }
}
