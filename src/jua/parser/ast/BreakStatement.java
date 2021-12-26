package jua.parser.ast;

public class BreakStatement extends Statement {

    public BreakStatement(Position position) {
        super(position);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBreak(this);
    }
}
