package jua.parser.ast;

public class TrueExpression extends BooleanExpression {

    public TrueExpression(Position position) {
        super(position);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitTrue(this);
    }
}
