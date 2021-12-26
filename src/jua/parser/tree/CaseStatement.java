package jua.parser.tree;

import java.util.List;

public class CaseStatement extends Statement {

    public List<Expression> expressions;

    public Statement body;

    public CaseStatement(Position position, List<Expression> expressions, Statement body) {
        super(Tag.CASE, position);
        this.expressions = expressions;
        this.body = body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCase(this);
    }
}
