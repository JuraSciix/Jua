package jua.parser.tree;

public abstract class BooleanExpression extends Expression {

    protected BooleanExpression(Position position) {
        super(Tag.LITERAL, position);
    }
}
