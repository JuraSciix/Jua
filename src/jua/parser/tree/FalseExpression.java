package jua.parser.tree;

public class FalseExpression extends BooleanExpression {

    public FalseExpression(Position position) {
        super(position);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitFalse(this);
    }
}
