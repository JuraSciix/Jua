package jua.parser.tree;

import java.util.Map;

public class ArrayExpression extends Expression {

    // todo: заменить это на List со своей структурой
    public Map<Expression, Expression> map;

    public ArrayExpression(Position position, Map<Expression, Expression> map) {
        super(Tag.ARRAY_LITERAL, position);
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
