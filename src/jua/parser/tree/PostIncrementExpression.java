package jua.parser.tree;

public class PostIncrementExpression extends IncreaseExpression {

    public PostIncrementExpression(Position position, Expression hs) {
        super(Tag.POST_INC, position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPostIncrement(this);
    }
}
