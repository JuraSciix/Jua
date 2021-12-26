package jua.parser.tree;

public class PositiveExpression extends UnaryExpression {

    public PositiveExpression(Position position, Expression hs) {
        super(Tag.POS, position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPositive(this);
    }
}
