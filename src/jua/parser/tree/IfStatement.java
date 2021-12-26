package jua.parser.tree;

public class IfStatement extends Statement {

    public Expression cond;

    public Statement body;

    public Statement elseBody;

    public IfStatement(Position position, Expression cond, Statement body) {
        this(position, cond, body, null);
    }

    public IfStatement(Position position, Expression cond, Statement body, Statement elseBody) {
        super(Tag.IFELSE, position);
        this.cond = cond;
        this.body = body;
        this.elseBody = elseBody;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitIf(this);
    }
}
