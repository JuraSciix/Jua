package jua.parser.ast;

import java.util.Map;

public class ArrayExpression extends Expression {

    // todo: заменить это на List со своей структурой
    public Map<Expression, Expression> map;

    public ArrayExpression(Position position, Map<Expression, Expression> map) {
        super(position);
        this.map = map;
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
    public void accept(Visitor visitor) {
        visitor.visitArray(this);
    }
}
