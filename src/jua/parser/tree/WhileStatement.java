package jua.parser.tree;

public class WhileStatement extends Statement {

    public Expression cond;

    public Statement body;

    public WhileStatement(Position position, Expression cond, Statement body) {
        super(Tag.WHILELOOP, position);
        this.cond = cond;
        this.body = body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitWhile(this);
    }
}
