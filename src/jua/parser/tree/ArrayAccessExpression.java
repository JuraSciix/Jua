package jua.parser.tree;

public class ArrayAccessExpression extends UnaryExpression {

    public Expression key;

    public ArrayAccessExpression(Position position, Expression hs, Expression key) {
        super(Tag.ARRAY_ACCESS, position, hs);
        this.key = key;
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public boolean isCloneable() {
        return true;
    }

    @Override
    public boolean isAssignable() {
        return true;
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitArrayAccess(this);
    }
}
