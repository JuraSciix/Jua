package jua.parser.ast;

public class TernaryExpression extends Expression {

    public Expression cond;

    public Expression lhs;

    public Expression rhs;

    public TernaryExpression(Position position, Expression cond, Expression lhs, Expression rhs) {
        super(position);
        this.cond = cond;
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

    @Override
    public boolean isNullable() {
        return lhs.isNullable() || rhs.isNullable();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitTernary(this);
    }
}
