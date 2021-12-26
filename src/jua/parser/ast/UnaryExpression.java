package jua.parser.ast;

public abstract class UnaryExpression extends Expression {

    public Expression hs;

    protected UnaryExpression(Position position, Expression hs) {
        super(position);
        this.hs = hs;
    }

    @Override
    public boolean isAccessible() {
        return hs.isAccessible();
    }
}
