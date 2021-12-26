package jua.parser;

import jua.parser.ast.Position;

public abstract class Token {

    public static class DummyToken extends Token {

        // used only for EOF
        public DummyToken(TokenType type, Position position) {
            super(type, position);
        }
    }

    public static class NamedToken extends Token {

        public NamedToken(TokenType type, Position position) {
            super(type, position);
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }

    public static class StringToken extends Token {

        protected final String value;

        public StringToken(TokenType type, Position position, String value) {
            super(type, position);
            this.value = value;
        }

        @Override
        public String getString() {
            return value;
        }

        @Override
        public String toString() {
            return '\'' + value + '\'';
        }
    }

    public static class NumberToken extends StringToken {

        protected final int radix;

        public NumberToken(TokenType type, Position position, String value, int radix) {
            super(type, position, value);
            this.radix = radix;
        }

        @Override
        public double getDouble() {
            return Double.parseDouble(value);
        }

        @Override
        public long getLong() {
            return Long.parseLong(value, radix);
        }
    }

    public final TokenType type;

    public final Position position;

    protected Token(TokenType type, Position position) {
        this.type = type;
        this.position = position;
    }

    public String getString() {
        return unsupported();
    }

    public double getDouble() {
        return unsupported();
    }

    public long getLong() {
        return unsupported();
    }

    private <T> T unsupported() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }
}
