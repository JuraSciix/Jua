package jua.parser.ast;

import java.util.List;

public class ArrayAccessExpression extends UnaryExpression {

    public List<Expression> keys;

    public ArrayAccessExpression(Position position, Expression hs, List<Expression> keys) {
        super(position, hs);
        this.keys = keys;
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
