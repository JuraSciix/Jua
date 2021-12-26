package jua.parser.ast;

import java.util.List;

public class PrintStatement extends Statement {

    public List<Expression> expressions;

    public PrintStatement(Position position, List<Expression> expressions) {
        super(position);
        this.expressions = expressions;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPrint(this);
    }
}
