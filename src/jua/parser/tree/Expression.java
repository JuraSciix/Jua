package jua.parser.tree;

public abstract class Expression extends Statement {

    public static Expression empty() {
        return new Expression(Tag.EMPTY, null) {

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public void accept(Visitor visitor) { }
        };
    }

    protected Expression(Tag tag, Position position) {
        super(tag, position);
    }

    // todo: Почти все эти методы лишние, часть из них нужно переместить в jua.compiler.TreeInfo

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
