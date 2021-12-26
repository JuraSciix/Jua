package jua.parser.tree;

public class PreIncrementExpression extends IncreaseExpression {

    public PreIncrementExpression(Position position, Expression hs) {
        super(Tag.PRE_INC, position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPreIncrement(this);
    }
}
