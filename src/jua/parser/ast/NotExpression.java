package jua.parser.ast;

public class NotExpression extends UnaryExpression {

    public NotExpression(Position position, Expression hs) {
        super(position, hs);
    }

    @Override
    public boolean isCondition() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNot(this);
    }
}
