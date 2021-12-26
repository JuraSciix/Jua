package jua.parser.tree;

public abstract class Statement extends Tree implements Cloneable {

    public static final Statement EMPTY = new Statement(Tag.EMPTY, null) {
        @Override
        public void accept(Visitor visitor) {}
    };

    protected Statement(Tag tag, Position position) {
        super(tag, position);
    }

    public Statement copy(Position position) {
        try {
            Statement clone = (Statement) super.clone();
            clone.position = position;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
