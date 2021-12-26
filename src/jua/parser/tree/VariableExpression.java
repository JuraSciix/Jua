package jua.parser.tree;

public class VariableExpression extends Expression {

    // todo: Заменить это на свою структуру (механизм уже готов, его нужно только внедрить)
    public final String name;

    public VariableExpression(Position position, String name) {
        super(Tag.VARIABLE, position);
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
