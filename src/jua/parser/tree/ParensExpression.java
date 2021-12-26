package jua.parser.tree;

public class ParensExpression extends Expression {

    public Expression expr;

    public ParensExpression(Position position, Expression expr) {
        super(Tag.PARENS, position);
        this.expr = expr;
    }

    @Override
    public Expression child() {
        return expr.child();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitParens(this);
    }
}
