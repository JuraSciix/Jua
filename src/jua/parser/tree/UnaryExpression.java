package jua.parser.tree;

public abstract class UnaryExpression extends Expression {

    public Expression hs;

    protected UnaryExpression(Tag tag, Position position, Expression hs) {
        super(tag, position);
        this.hs = hs;
    }

    @Override
    public boolean isAccessible() {
        return hs.isAccessible();
    }
}
