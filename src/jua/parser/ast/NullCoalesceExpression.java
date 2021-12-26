package jua.parser.ast;

public class NullCoalesceExpression extends BinaryExpression {

    public NullCoalesceExpression(Position position, Expression lhs, Expression rhs) {
        super(position, lhs, rhs);
    }

    @Override
    public boolean isAccessible() {
        return rhs.isAccessible();
    }

    @Override
    public boolean isCloneable() {
        return rhs.isCloneable();
    }

    @Override
    public boolean isNullable() {
        return lhs.isNullable();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNullCoalesce(this);
    }
}
