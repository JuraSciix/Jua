package jua.parser.ast;

public class FallthroughStatement extends Statement {

    public FallthroughStatement(Position position) {
        super(position);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitFallthrough(this);
    }
}
