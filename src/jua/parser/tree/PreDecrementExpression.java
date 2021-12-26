package jua.parser.tree;

public class PreDecrementExpression extends IncreaseExpression {

    public PreDecrementExpression(Position position, Expression hs) {
        super(Tag.PRE_DEC, position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPreDecrement(this);
    }
}
