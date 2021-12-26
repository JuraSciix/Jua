package jua.parser.ast;

import java.util.List;

public class FunctionCallExpression extends Expression {

    public final String name;

    public List<Expression> args;

    public FunctionCallExpression(Position position, String name, List<Expression> args) {
        super(position);
        this.name = name;
        this.args = args;
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
    public boolean isNullable() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitFunctionCall(this);
    }
}
