package jua.parser.ast;

public class BitNotExpression extends UnaryExpression {

    public BitNotExpression(Position position, Expression hs) {
        super(position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBitNot(this);
    }
}
