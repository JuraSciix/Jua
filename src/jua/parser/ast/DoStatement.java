package jua.parser.ast;

public class DoStatement extends Statement {

    public Statement body;

    public Expression cond;

    public DoStatement(Position position, Statement body, Expression cond) {
        super(position);
        this.body = body;
        this.cond = cond;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitDo(this);
    }
}
