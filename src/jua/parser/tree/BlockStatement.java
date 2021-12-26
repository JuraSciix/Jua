package jua.parser.tree;

import java.util.List;

public class BlockStatement extends Statement {

    public List<Statement> statements;

    public BlockStatement(Position position, List<Statement> statements) {
        super(Tag.COMPOUND, position);
        this.statements = statements;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBlock(this);
    }
}
