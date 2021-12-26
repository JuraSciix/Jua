package jua.parser.tree;

public class CloneExpression extends UnaryExpression {

    public CloneExpression(Position position, Expression hs) {
        super(Tag.CLONE, position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitClone(this);
    }
}
