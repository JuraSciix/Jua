package jua.parser.ast;

public abstract class Statement implements Node, Cloneable {

    public static final Statement EMPTY = new Statement(null) {
        @Override
        public void accept(Visitor visitor) {}
    };

    private Position position;

    protected Statement(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
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
