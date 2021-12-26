package jua.parser.ast;

public class IntExpression extends Expression {

    public long value;

    public IntExpression(Position position, long value) {
        super(position);
        this.value = value;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitInt(this);
    }
}
