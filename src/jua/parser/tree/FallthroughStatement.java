package jua.parser.tree;

public class FallthroughStatement extends Statement {

    public FallthroughStatement(Position position) {
        super(Tag.FALLTHROUGH, position);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitFallthrough(this);
    }
}
