package jua.parser.ast;

public class VariableExpression extends Expression {

    public final String name;

    public VariableExpression(Position position, String name) {
        super(position);
        this.name = name;
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public boolean isAssignable() {
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
        visitor.visitVariable(this);
    }
}
