package jua.compiler;

import jua.compiler.Tree.Literal;
import jua.compiler.Tree.Tag;

import java.util.HashMap;

import static jua.compiler.SemanticInfo.ofBoolean;

@SuppressWarnings("Convert2MethodRef")
public final class Operators {

    @FunctionalInterface
    private interface BinaryOperator<X, Y> {
        Object apply(X x, Y y);
    }

    @FunctionalInterface
    private interface UnaryOperator<X> {
        Object apply(X x);
    }

    private static class BinaryOperatorKey {
        final Tag tag; // Никогда не равен нулю.
        final Class<?> xt, yt; // Никогда не равны нулю.

        BinaryOperatorKey(Tag tag, Class<?> xt, Class<?> yt) {
            this.tag = tag;
            this.xt = xt;
            this.yt = yt;
        }

        @Override
        public int hashCode() {
            return 29791 + tag.hashCode() * 961 + xt.hashCode() * 31 + yt.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            // [this == o] никогда не верно
            if (o == null || getClass() != o.getClass()) return false;
            BinaryOperatorKey k = (BinaryOperatorKey) o;
            return tag == k.tag && xt.equals(k.xt) && yt.equals(k.yt);
        }
    }

    private static class UnaryOperatorKey {
        final Tag tag; // Никогда не равен нулю
        final Class<?> xt; // Никогда не равен нулю

        UnaryOperatorKey(Tag tag, Class<?> xt) {
            this.tag = tag;
            this.xt = xt;
        }

        @Override
        public int hashCode() {
            return 961 + tag.hashCode() * 31 + xt.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            // [this == o] никогда не верно
            if (o == null || getClass() != o.getClass()) return false;
            UnaryOperatorKey k = (UnaryOperatorKey) o;
            return tag == k.tag && xt.equals(k.xt);
        }
    }

    private static Class<?> getClass0(Object o) {
        return (o != null) ? o.getClass() : Object.class;
    }

    private final HashMap<BinaryOperatorKey, BinaryOperator<?, ?>> binaryOperators = new HashMap<>();
    private final HashMap<UnaryOperatorKey, UnaryOperator<?>> unaryOperators = new HashMap<>();

    public Operators() {
        // Тип Object включает null.

        addBinaryOperator(Tag.ADD, Long.class, Long.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, Double.class, Number.class, (x, y) -> x + y.doubleValue());
        addBinaryOperator(Tag.ADD, Number.class, Double.class, (x, y) -> x.doubleValue() + y);
        addBinaryOperator(Tag.ADD, String.class, Object.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, Object.class, String.class, (x, y) -> x + y);

        addBinaryOperator(Tag.SUB, Long.class, Long.class, (x, y) -> x - y);
        addBinaryOperator(Tag.SUB, Double.class, Number.class, (x, y) -> x - y.doubleValue());
        addBinaryOperator(Tag.SUB, Number.class, Double.class, (x, y) -> x.doubleValue() - y);

        addBinaryOperator(Tag.MUL, Long.class, Long.class, (x, y) -> x * y);
        addBinaryOperator(Tag.MUL, Double.class, Number.class, (x, y) -> x * y.doubleValue());
        addBinaryOperator(Tag.MUL, Number.class, Double.class, (x, y) -> x.doubleValue() * y);

        addBinaryOperator(Tag.DIV, Long.class, Long.class, (x, y) -> x / y);
        addBinaryOperator(Tag.DIV, Double.class, Number.class, (x, y) -> x / y.doubleValue());
        addBinaryOperator(Tag.DIV, Number.class, Double.class, (x, y) -> x.doubleValue() / y);

        addBinaryOperator(Tag.REM, Long.class, Long.class, (x, y) -> x % y);
        addBinaryOperator(Tag.REM, Double.class, Number.class, (x, y) -> x % y.doubleValue());
        addBinaryOperator(Tag.REM, Number.class, Double.class, (x, y) -> x.doubleValue() % y);

        addBinaryOperator(Tag.BIT_AND, Long.class, Long.class, (x, y) -> x & y);

        addBinaryOperator(Tag.BIT_OR, Long.class, Long.class, (x, y) -> x | y);

        addBinaryOperator(Tag.BIT_XOR, Long.class, Long.class, (x, y) -> x ^ y);

        addBinaryOperator(Tag.SL, Long.class, Long.class, (x, y) -> x << y);

        addBinaryOperator(Tag.SR, Long.class, Long.class, (x, y) -> x >> y);

        addBinaryOperator(Tag.GT, Long.class, Long.class, (x, y) -> x > y);
        addBinaryOperator(Tag.GT, Double.class, Number.class, (x, y) -> x > y.doubleValue());
        addBinaryOperator(Tag.GT, Number.class, Double.class, (x, y) -> x.doubleValue() > y);

        addBinaryOperator(Tag.GE, Long.class, Long.class, (x, y) -> x >= y);
        addBinaryOperator(Tag.GE, Double.class, Number.class, (x, y) -> x >= y.doubleValue());
        addBinaryOperator(Tag.GE, Number.class, Double.class, (x, y) -> x.doubleValue() >= y);

        addBinaryOperator(Tag.LT, Long.class, Long.class, (x, y) -> x < y);
        addBinaryOperator(Tag.LT, Double.class, Number.class, (x, y) -> x < y.doubleValue());
        addBinaryOperator(Tag.LT, Number.class, Double.class, (x, y) -> x.doubleValue() < y);

        addBinaryOperator(Tag.LE, Long.class, Long.class, (x, y) -> x <= y);
        addBinaryOperator(Tag.LE, Number.class, Double.class, (x, y) -> x.doubleValue() <= y);
        addBinaryOperator(Tag.LE, Double.class, Number.class, (x, y) -> x <= y.doubleValue());

        addBinaryOperator(Tag.EQ, Long.class, Long.class, (x, y) -> x.longValue() == y.longValue());
        addBinaryOperator(Tag.EQ, Double.class, Number.class, (x, y) -> x == y.doubleValue());
        addBinaryOperator(Tag.EQ, Number.class, Double.class, (x, y) -> x.doubleValue() == y);

        addBinaryOperator(Tag.NE, Long.class, Long.class, (x, y) -> x.longValue() != y.longValue());
        addBinaryOperator(Tag.NE, Double.class, Number.class, (x, y) -> x != y.doubleValue());
        addBinaryOperator(Tag.NE, Number.class, Double.class, (x, y) -> x.doubleValue() != y);

        addBinaryOperator(Tag.AND, Object.class, Object.class, (x, y) -> ofBoolean(x).toBoolean() && ofBoolean(y).toBoolean());
        addBinaryOperator(Tag.OR, Object.class, Object.class, (x, y) -> ofBoolean(x).toBoolean() || ofBoolean(y).toBoolean());

        addUnaryOperator(Tag.POS, Long.class, x -> x);
        addUnaryOperator(Tag.POS, Double.class, x -> x);

        addUnaryOperator(Tag.NEG, Long.class, x -> -x);
        addUnaryOperator(Tag.NEG, Double.class, x -> -x);

        addUnaryOperator(Tag.BIT_INV, Long.class, x -> ~x);

        addUnaryOperator(Tag.NOT, Boolean.class, x -> !x);

        addUnaryOperator(Tag.NULLCHK, Object.class, x -> x != null);
    }

    private <L, R> void addBinaryOperator(Tag tag, Class<L> lt, Class<R> rt, BinaryOperator<L, R> operator) {
        binaryOperators.put(new BinaryOperatorKey(tag, lt, rt), operator);
    }

    private <A> void addUnaryOperator(Tag tag, Class<A> t, UnaryOperator<A> operator) {
        unaryOperators.put(new UnaryOperatorKey(tag, t), operator);
    }

    @SuppressWarnings("unchecked")
    public <X, Y> Tree applyBinaryOperator(Tag tag, int pos, X x, Y y, Tree tree) {
        BinaryOperator<X, Y> operator = (BinaryOperator<X, Y>) findBinaryOperator(tag, getClass0(x), getClass0(y));

        if (operator == null) {
            return tree;
        }

        try {
            return new Literal(pos, operator.apply(x, y));
        } catch (ArithmeticException e) {
            return tree;
        }
    }

    private BinaryOperator<?, ?> findBinaryOperator(Tag tag, Class<?> xt, Class<?> yt) {
        BinaryOperatorKey key = new BinaryOperatorKey(tag, xt, yt);
        if (binaryOperators.containsKey(key)) return binaryOperators.get(key);
        if (xt != Object.class) {
            BinaryOperator<?, ?> operator = findBinaryOperator(tag, xt.getSuperclass(), yt);
            if (operator != null) return operator;
        }
        if (yt != Object.class) {
            return findBinaryOperator(tag, xt, yt.getSuperclass());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <X> Tree applyUnaryOperator(Tag tag, int pos, X x, Tree tree) {
        UnaryOperator<X> operator = (UnaryOperator<X>) findUnaryOperator(tag, getClass0(x));

        if (operator == null) {
            return tree;
        }

        return new Literal(pos, operator.apply(x));
    }

    private UnaryOperator<?> findUnaryOperator(Tag tag, Class<?> xt) {
        UnaryOperatorKey key = new UnaryOperatorKey(tag, xt);
        if (unaryOperators.containsKey(key)) return unaryOperators.get(key);
        if (xt != Object.class) {
            return findUnaryOperator(tag, xt.getSuperclass());
        }
        return null;
    }
}
