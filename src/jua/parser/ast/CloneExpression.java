package jua.parser.ast;

public class CloneExpression extends UnaryExpression {

    public CloneExpression(Position position, Expression hs) {
        super(position, hs);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitClone(this);
    }
}
