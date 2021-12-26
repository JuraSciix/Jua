package jua.parser.tree;

public class BitXorExpression extends BinaryExpression {

    public BitXorExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.BITXOR, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBitXor(this);
    }
}
