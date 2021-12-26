package jua.parser.tree;

public class UnusedExpression extends Expression {

    public Expression expression;

    public UnusedExpression(Position position, Expression expression) {
        super(Tag.UNUSED, position);
        this.expression = expression;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitUnused(this);
    }
}
