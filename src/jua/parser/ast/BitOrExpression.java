package jua.parser.ast;

public class BitOrExpression extends BinaryExpression {

    public BitOrExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBitOr(this);
    }
}
