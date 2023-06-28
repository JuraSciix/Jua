package jua.compiler;

import jua.compiler.Tree.Literal;
import jua.compiler.Tree.Tag;

import java.util.HashMap;
import java.util.Objects;

import static jua.compiler.SemanticInfo.ofBoolean;

public class Operators {

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
        final Class<?> xt, yt; // могут быть равны нулю

        BinaryOperatorKey(Tag tag, Class<?> xt, Class<?> yt) {
            this.tag = tag;
            this.xt = xt;
            this.yt = yt;
        }

        @Override
        public int hashCode() {
            return 29791 + tag.hashCode() * 961 + Objects.hashCode(xt) * 31 + Objects.hashCode(yt);
        }

        @Override
        public boolean equals(Object o) {
            // [this == o] никогда не верно
            if (o == null || getClass() != o.getClass()) return false;
            BinaryOperatorKey k = (BinaryOperatorKey) o;
            return tag == k.tag && Objects.equals(xt, k.xt) && Objects.equals(yt, k.yt);
        }
    }

    private static class UnaryOperatorKey {
        final Tag tag;
        final Class<?> xt;

        UnaryOperatorKey(Tag tag, Class<?> xt) {
            this.tag = tag;
            this.xt = xt;
        }

        @Override
        public int hashCode() {
            return 961 + tag.hashCode() * 31 + Objects.hashCode(xt);
        }

        @Override
        public boolean equals(Object o) {
            // [this == o] никогда не верно
            if (o == null || getClass() != o.getClass()) return false;
            UnaryOperatorKey k = (UnaryOperatorKey) o;
            return tag == k.tag && Objects.equals(xt, k.xt);
        }
    }

    private static Class<?> getClass0(Object o) {
        return (o != null) ? o.getClass() : null;
    }

    private static Class<?> getSuperClass(Class<?> c) {
        return (c != null && c != Object.class) ? c.getSuperclass() : null;
    }

    private final HashMap<BinaryOperatorKey, BinaryOperator<?, ?>> binaryOperators = new HashMap<>();
    private final HashMap<UnaryOperatorKey, UnaryOperator<?>> unaryOperators = new HashMap<>();

    public Operators() {
        addBinaryOperator(Tag.ADD, Long.class, Long.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, Long.class, Double.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, Long.class, String.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, Double.class, Long.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, Double.class, Double.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, Double.class, String.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, String.class, Long.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, String.class, Double.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, String.class, String.class, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, String.class, null, (x, y) -> x + y);
        addBinaryOperator(Tag.ADD, null, String.class, (x, y) -> x + y);

        addBinaryOperator(Tag.SUB, Long.class, Long.class, (x, y) -> x - y);
        addBinaryOperator(Tag.SUB, Long.class, Double.class, (x, y) -> x - y);
        addBinaryOperator(Tag.SUB, Double.class, Long.class, (x, y) -> x - y);
        addBinaryOperator(Tag.SUB, Double.class, Double.class, (x, y) -> x - y);

        addBinaryOperator(Tag.MUL, Long.class, Long.class, (x, y) -> x * y);
        addBinaryOperator(Tag.MUL, Long.class, Double.class, (x, y) -> x * y);
        addBinaryOperator(Tag.MUL, Double.class, Long.class, (x, y) -> x * y);
        addBinaryOperator(Tag.MUL, Double.class, Double.class, (x, y) -> x * y);

        addBinaryOperator(Tag.DIV, Long.class, Long.class, (x, y) -> x / y);
        addBinaryOperator(Tag.DIV, Long.class, Double.class, (x, y) -> x / y);
        addBinaryOperator(Tag.DIV, Double.class, Long.class, (x, y) -> x / y);
        addBinaryOperator(Tag.DIV, Double.class, Double.class, (x, y) -> x / y);

        addBinaryOperator(Tag.REM, Long.class, Long.class, (x, y) -> x % y);
        addBinaryOperator(Tag.REM, Long.class, Double.class, (x, y) -> x % y);
        addBinaryOperator(Tag.REM, Double.class, Long.class, (x, y) -> x % y);
        addBinaryOperator(Tag.REM, Double.class, Double.class, (x, y) -> x % y);

        addBinaryOperator(Tag.BIT_AND, Long.class, Long.class, (x, y) -> x & y);

        addBinaryOperator(Tag.BIT_OR, Long.class, Long.class, (x, y) -> x | y);

        addBinaryOperator(Tag.BIT_XOR, Long.class, Long.class, (x, y) -> x ^ y);

        addBinaryOperator(Tag.SL, Long.class, Long.class, (x, y) -> x << y);

        addBinaryOperator(Tag.SR, Long.class, Long.class, (x, y) -> x >> y);

        addBinaryOperator(Tag.GT, Long.class, Long.class, (x, y) -> x > y);
        addBinaryOperator(Tag.GT, Long.class, Double.class, (x, y) -> x > y);
        addBinaryOperator(Tag.GT, Double.class, Long.class, (x, y) -> x > y);
        addBinaryOperator(Tag.GT, Double.class, Double.class, (x, y) -> x > y);

        addBinaryOperator(Tag.GE, Long.class, Long.class, (x, y) -> x >= y);
        addBinaryOperator(Tag.GE, Long.class, Double.class, (x, y) -> x >= y);
        addBinaryOperator(Tag.GE, Double.class, Long.class, (x, y) -> x >= y);
        addBinaryOperator(Tag.GE, Double.class, Double.class, (x, y) -> x >= y);

        addBinaryOperator(Tag.LT, Long.class, Long.class, (x, y) -> x < y);
        addBinaryOperator(Tag.LT, Long.class, Double.class, (x, y) -> x < y);
        addBinaryOperator(Tag.LT, Double.class, Long.class, (x, y) -> x < y);
        addBinaryOperator(Tag.LT, Double.class, Double.class, (x, y) -> x < y);

        addBinaryOperator(Tag.LE, Long.class, Long.class, (x, y) -> x <= y);
        addBinaryOperator(Tag.LE, Long.class, Double.class, (x, y) -> x <= y);
        addBinaryOperator(Tag.LE, Double.class, Long.class, (x, y) -> x <= y);
        addBinaryOperator(Tag.LE, Double.class, Double.class, (x, y) -> x <= y);

        addBinaryOperator(Tag.EQ, Long.class, Long.class, (x, y) -> x.longValue() == y.longValue());
        addBinaryOperator(Tag.EQ, Long.class, Double.class, (x, y) -> x.doubleValue() == y);
        addBinaryOperator(Tag.EQ, Double.class, Long.class, (x, y) -> x == y.doubleValue());
        addBinaryOperator(Tag.EQ, Double.class, Double.class, (x, y) -> x.doubleValue() == y.doubleValue());

        addBinaryOperator(Tag.NE, Long.class, Long.class, (x, y) -> x.longValue() != y.longValue());
        addBinaryOperator(Tag.NE, Long.class, Double.class, (x, y) -> x.doubleValue() != y);
        addBinaryOperator(Tag.NE, Double.class, Long.class, (x, y) -> x != y.doubleValue());
        addBinaryOperator(Tag.NE, Double.class, Double.class, (x, y) -> x.doubleValue() != y.doubleValue());

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
        if (xt != null && xt != Object.class) {
            BinaryOperator<?, ?> operator = findBinaryOperator(tag, xt.getSuperclass(), yt);
            if (operator != null) return operator;
        }
        if (yt != null && yt != Object.class) {
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
        if (xt != null && xt != Object.class) {
            return findUnaryOperator(tag, xt.getSuperclass());
        }
        return null;
    }
}
