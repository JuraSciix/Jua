package jua.parser.ast;

public abstract class AssignmentExpression extends Expression {

    public Expression var;

    public Expression expr;

    protected AssignmentExpression(Position position, Expression var, Expression expr) {
        super(position);
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
