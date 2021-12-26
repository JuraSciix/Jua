package jua.parser.ast;

import java.util.List;

public class ConstantDeclareStatement extends Statement {

    public List<String> names;

    public List<Expression> expressions;

    public ConstantDeclareStatement(Position position, List<String> names, List<Expression> expressions) {
        super(position);
        this.names = names;
        this.expressions = expressions;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitConstantDeclare(this);
    }
}
