package jua.parser.tree;

public class DoStatement extends Statement {

    public Statement body;

    public Expression cond;

    public DoStatement(Position position, Statement body, Expression cond) {
        super(Tag.DOLOOP, position);
        this.body = body;
        this.cond = cond;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitDo(this);
    }
}
