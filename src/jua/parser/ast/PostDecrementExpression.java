package jua.parser.ast;

public class PostDecrementExpression extends IncreaseExpression {

    public PostDecrementExpression(Position position, Expression hs) {
        super(position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPostDecrement(this);
    }
}
