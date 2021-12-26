package jua.parser.ast;

public class BitXorExpression extends BinaryExpression {

    public BitXorExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBitXor(this);
    }
}
