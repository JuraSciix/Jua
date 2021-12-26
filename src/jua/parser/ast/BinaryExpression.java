package jua.parser.ast;

public abstract class BinaryExpression extends Expression {

    public Expression lhs;

    public Expression rhs;

    protected BinaryExpression(Position position, Expression lhs, Expression rhs) {
        super(position);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean isAccessible() {
        return lhs.isAccessible() && rhs.isAccessible();
    }

    @Override
    public boolean isCloneable() {
        return lhs.isCloneable() && rhs.isCloneable();
    }
}
