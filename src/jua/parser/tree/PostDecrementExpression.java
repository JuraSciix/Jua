package jua.parser.tree;

public class PostDecrementExpression extends IncreaseExpression {

    public PostDecrementExpression(Position position, Expression hs) {
        super(Tag.POST_DEC, position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPostDecrement(this);
    }
}
