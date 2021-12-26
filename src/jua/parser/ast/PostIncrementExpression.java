package jua.parser.ast;

public class PostIncrementExpression extends IncreaseExpression {

    public PostIncrementExpression(Position position, Expression hs) {
        super(position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPostIncrement(this);
    }
}
