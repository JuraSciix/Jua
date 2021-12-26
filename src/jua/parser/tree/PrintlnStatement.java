package jua.parser.tree;

import java.util.List;

public class PrintlnStatement extends Statement {

    public List<Expression> expressions;

    public PrintlnStatement(Position position, List<Expression> expressions) {
        super(Tag.PRINTLN, position);
        this.expressions = expressions;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPrintln(this);
    }
}
