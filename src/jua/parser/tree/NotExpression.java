package jua.parser.tree;

public class NotExpression extends UnaryExpression {

    public NotExpression(Position position, Expression hs) {
        super(Tag.LOGCMPL, position, hs);
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
