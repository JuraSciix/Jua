package jua.parser.ast;

public class PreIncrementExpression extends IncreaseExpression {

    public PreIncrementExpression(Position position, Expression hs) {
        super(position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPreIncrement(this);
    }
}
