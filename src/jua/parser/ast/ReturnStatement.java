package jua.parser.ast;

public class ReturnStatement extends Statement {

    public Expression expr;

    public ReturnStatement(Position position) {
        this(position, null);
    }

    public ReturnStatement(Position position, Expression expr) {
        super(position);
        this.expr = expr;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitReturn(this);
    }
}
