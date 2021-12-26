package jua.parser.tree;

public class BitOrExpression extends BinaryExpression {

    public BitOrExpression(Position position, Expression lhs, Expression rhs) {
        super(Tag.BITOR, position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBitOr(this);
    }
}
