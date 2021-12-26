package jua.parser.ast;

import java.util.List;

public class FunctionDefineStatement extends Statement {

    public final String name;

    public final List<String> names;

    public List<Expression> optionals;

    public Statement body;

    public FunctionDefineStatement(Position position,
                                   String name,
                                   List<String> names,
                                   List<Expression> optionals,
                                   Statement body) {
        super(position);
        this.name = name;
        this.names = names;
        this.optionals = optionals;
        this.body = body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitFunctionDefine(this);
    }
}
