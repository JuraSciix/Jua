package jua.parser.tree;

import java.util.List;

public class PrintlnStatement extends PrintStatement {

    public PrintlnStatement(Position position, List<Expression> expressions) {
        super(position, expressions);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPrintln(this);
    }
}
