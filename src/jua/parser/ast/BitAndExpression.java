package jua.parser.ast;

public class BitAndExpression extends BinaryExpression {

    public BitAndExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBitAnd(this);
    }
}
