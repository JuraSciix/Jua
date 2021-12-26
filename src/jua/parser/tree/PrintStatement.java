package jua.parser.tree;

import java.util.List;

// todo: Заменить это на вызов функций с соответствующими именами
public class PrintStatement extends Statement {

    public List<Expression> expressions;

    public PrintStatement(Position position, List<Expression> expressions) {
        // print и println с какой-то там версии больше не являются языковыми конструкциями.
        super(Tag.FUNC_CALL, position);
        this.expressions = expressions;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPrint(this);
    }
}
