package jua.parser.ast;

public class NullExpression extends Expression {

    public NullExpression(Position position) {
        super(position);
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
