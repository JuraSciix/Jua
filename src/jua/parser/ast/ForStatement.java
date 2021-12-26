package jua.parser.ast;

import java.util.List;

public class ForStatement extends Statement {

    public List<Expression> init;

    public Expression cond;

    public List<Expression> step;

    public Statement body;

    public ForStatement(Position position, List<Expression> init, Expression cond, List<Expression> step, Statement body) {
        super(position);
        this.init = init;
        this.cond = cond;
        this.step = step;
        this.body = body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitFor(this);
    }
}
