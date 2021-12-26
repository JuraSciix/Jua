package jua.parser.ast;

public class PreDecrementExpression extends IncreaseExpression {

    public PreDecrementExpression(Position position, Expression hs) {
        super(position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPreDecrement(this);
    }
}
