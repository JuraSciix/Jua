package jua.parser.tree;

public class FloatExpression extends Expression {

    public double value;

    public FloatExpression(Position position, double value) {
        super(Tag.LITERAL, position);
        this.value = value;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitFloat(this);
    }
}
