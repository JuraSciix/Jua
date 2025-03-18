package jua.compiler;

import jua.compiler.utils.EnumMath;

import java.util.HashMap;
import java.util.Map;

public final class Tokens {

    public enum TokenType {
        // BINARY OPERATORS
        PLUS("+"),
        MINUS("-"),
        STAR("*"),
        SLASH("/"),
        PERCENT("%"),
        AMP("&"),
        BAR("|"),
        CARET("^"),
        GTGT(">>"),
        LTLT("<<"),
        GT(">"),
        GTEQ(">="),
        LT("<"),
        LTEQ("<="),
        EQEQ("=="),
        BANGEQ("!="),
        AMPAMP("&&"),
        BARBAR("||"),
        QUESQUES("??"),

        // UNARY OPERATORS,
        BANG("!"),
        TILDE("~"),
        MINUSMINUS("--"),
        PLUSPLUS("++"),
        AT("@"),

        EQ("="),
        // ENHANCED ASG OPERATORS,
        PLUSEQ("+="),
        MINUSEQ("-="),
        STAREQ("*="),
        SLASHEQ("/="),
        PERCENTEQ("%="),
        AMPEQ("&="),
        BAREQ("|="),
        CARETEQ("^="),
        GTGTEQ(">>="),
        LTLTEQ("<<="),
        QUESQUESEQ("??="),
        COLEQ(":="),

        // STATEMENT KEYWORDS
        BREAK("break"),
        CONST("const"),
        CONTINUE("continue"),
        DO("do"),
        ELSE("else"),
        FALLTHROUGH("fallthrough"),
        FALSE("false"),
        FN("fn"),
        FOR("for"),
        IF("if"),
        RETURN("return"),
        SWITCH("switch"),
        TRUE("true"),
        VAR("var"),
        WHILE("while"),
        YIELD("yield"),
        ONCE("once"),

        // EXPRESSION KEYWORDS
        NULL("null"),

        ARROW("->"),
        COL(":"),
        COMMA(","),
        CUSTOM,
        DOT("."),
        LBRACE("{"),
        LBRACKET("["),
        LPAREN("("),
        QUES("?"),
        RBRACE("}"),
        RBRACKET("]"),
        RPAREN(")"),
        SEMI(";"),
        INVALID,

        STRINGLITERAL(),
        FLOATLITERAL(),
        IDENTIFIER(),
        INTLITERAL(),

        EOF;

        private static final Map<String, TokenType> LOOKUP = new HashMap<>(16, 1f);

        static {
            for (TokenType type : values()) {
                if (type != null) {
                    LOOKUP.put(type.value, type);
                }
            }
        }

        public static TokenType lookup(String value) {
            return LOOKUP.get(value);
        }

        public final String value;

        TokenType() {
            this(null);
        }

        TokenType(String value) {
            this.value = value;
        }

        TokenKind kind() {
            if (EnumMath.between(this, PLUS, INVALID))
                return TokenKind.DEFAULT;
            if (this == STRINGLITERAL)
                return TokenKind.STRING;
            if (this == INTLITERAL || this == FLOATLITERAL)
                return TokenKind.NUMERIC;
            if (this == IDENTIFIER)
                return TokenKind.NAMED;
            throw new AssertionError(this);
        }
    }

    public enum TokenKind {
        DEFAULT,
        NAMED,
        NUMERIC,
        STRING
    }

    public static class Token {

        public final TokenType type;

        public final int pos;

        Token(TokenType type, int pos) {
            this.type = type;
            this.pos = pos;
        }

        public String name() { throw unsupportedOperationException(); }

        public String value() { throw unsupportedOperationException(); }

        public int radix() { throw unsupportedOperationException(); }

        private UnsupportedOperationException unsupportedOperationException() {
            return new UnsupportedOperationException(type.name());
        }
    }

    public static class NamedToken extends Token {

        public final String name;

        public NamedToken(TokenType type, int pos, String name) {
            super(type, pos);
            this.name = name;
        }

        @Override
        public String name() { return name; }
    }

    public static class StringToken extends Token {

        public final String value;

        public StringToken(TokenType type, int pos, String value) {
            super(type, pos);
            this.value = value;
        }

        @Override
        public String value() { return value; }
    }

    public static class NumericToken extends StringToken {

        public final int radix;

        public NumericToken(TokenType type, int pos, String value, int radix) {
            super(type, pos, value);
            this.radix = radix;
        }

        @Override
        public int radix() { return radix; }
    }

    private Tokens() { throw new UnsupportedOperationException("Instantiation this class is useless"); }
}
