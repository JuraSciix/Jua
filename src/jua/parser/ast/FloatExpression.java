package jua.parser.ast;

public class FloatExpression extends Expression {

    public double value;

    public FloatExpression(Position position, double value) {
        super(position);
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
