package jua.parser.tree;

public abstract class AssignmentExpression extends Expression {

    public Expression var;

    public Expression expr;

    protected AssignmentExpression(Tag tag, Position position, Expression var, Expression expr) {
        super(tag, position);
        this.var = var;
        this.expr = expr;
    }

    @Override
    public boolean isAccessible() {
        return expr.isAccessible();
    }

    @Override
    public boolean isCloneable() {
        return expr.isCloneable();
    }

    @Override
    public boolean isNullable() {
        return expr.isNullable();
    }
}
