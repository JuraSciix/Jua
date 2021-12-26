package jua.parser.tree;

public class NegativeExpression extends UnaryExpression {

    public NegativeExpression(Position position, Expression hs) {
        super(Tag.NEG, position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNegative(this);
    }
}
