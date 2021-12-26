package jua.parser.tree;

public class NullExpression extends Expression {

    public NullExpression(Position position) {
        super(Tag.LITERAL, position);
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNull(this);
    }
}
