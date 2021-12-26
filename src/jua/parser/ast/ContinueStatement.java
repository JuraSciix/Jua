package jua.parser.ast;

public class ContinueStatement extends Statement {

    public ContinueStatement(Position position) {
        super(position);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitContinue(this);
    }
}
