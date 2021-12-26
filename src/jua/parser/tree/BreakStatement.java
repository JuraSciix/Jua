package jua.parser.tree;

public class BreakStatement extends Statement {

    public BreakStatement(Position position) {
        super(Tag.BREAK, position);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBreak(this);
    }
}
