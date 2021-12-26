package jua.parser.tree;

import java.util.List;

public class FunctionDefineStatement extends Statement {

    // todo: Заменить строки на свои структуры (механизм уже готов, его нужно только внедрить)

    public final String name;

    public final List<String> names;

    public List<Expression> optionals;

    public Statement body;

    public FunctionDefineStatement(Position position,
                                   String name,
                                   List<String> names,
                                   List<Expression> optionals,
                                   Statement body) {
        super(Tag.FUNCDECL, position);
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
