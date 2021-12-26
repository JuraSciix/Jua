package jua.parser.tree;

public class BitAndExpression extends BinaryExpression {

    public BitAndExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.BITAND, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBitAnd(this);
    }
}
