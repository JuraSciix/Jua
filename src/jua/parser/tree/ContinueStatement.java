package jua.parser.tree;

public class ContinueStatement extends Statement {

    public ContinueStatement(Position position) {
        super(Tag.CONTINUE, position);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitContinue(this);
    }
}
