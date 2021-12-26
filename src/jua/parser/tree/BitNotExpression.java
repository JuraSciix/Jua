package jua.parser.tree;

public class BitNotExpression extends UnaryExpression {

    public BitNotExpression(Position position, Expression hs) {
        super(Tag.BITCMPL, position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBitNot(this);
    }
}
