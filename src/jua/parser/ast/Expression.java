package jua.parser.ast;

public abstract class Expression extends Statement {

    public static Expression empty() {
        return new Expression(null) {

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public void accept(Visitor visitor) { }
        };
    }

    protected Expression(Position position) {
        super(position);
    }

    public boolean isAccessible() {
        return false;
    }

    public boolean isAssignable() {
        return false;
    }

    public boolean isCloneable() {
        return false;
    }

    public boolean isCondition() {
        return false;
    }

    public boolean isLiteral() {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isNullable() {
        return false;
    }

    public Expression child() {
        return this;
    }
}
