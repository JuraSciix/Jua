package jua.compiler;

import java.util.HashMap;
import java.util.Map;

public final class Tokens {

    public enum TokenType {
        AMP("&"),
        AMPAMP("&&"),
        AMPEQ("&="),
        ARROW("->"),
        AT("@"),
        BANG("!"),
        BANGEQ("!="),
        BAR("|"),
        BARBAR("||"),
        BAREQ("|="),
        BREAK("break"),
        CARET("^"),
        CARETEQ("^="),
        COL(":"),
        COMMA(","),
        CONST("const"),
        CONTINUE("continue"),
        CUSTOM,
        DO("do"),
        DOT("."),
        ELSE("else"),
        EOF,
        EQ("="),
        EQEQ("=="),
        FALLTHROUGH("fallthrough"),
        FALSE("false"),
        FLOATLITERAL(TokenKind.NUMERIC),
        FN("fn"),
        FOR("for"),
        GT(">"),
        GTEQ(">="),
        GTGT(">>"),
        GTGTEQ(">>="),
        IDENTIFIER(TokenKind.NAMED),
        IF("if"),
        INTLITERAL(TokenKind.NUMERIC),
        INVALID,
        LBRACE("{"),
        LBRACKET("["),
        LPAREN("("),
        LT("<"),
        LTEQ("<="),
        LTGT("<>"),
        LTLT("<<"),
        LTLTEQ("<<="),
        MINUS("-"),
        MINUSEQ("-="),
        MINUSMINUS("--"),
        NULL("null"),
        PERCENT("%"),
        PERCENTEQ("%="),
        PLUS("+"),
        PLUSEQ("+="),
        PLUSPLUS("++"),
        QUES("?"),
        QUESQUES("??"),
        QUESQUESEQ("??="),
        RBRACE("}"),
        RBRACKET("]"),
        RETURN("return"),
        RPAREN(")"),
        SEMI(";"),
        SLASH("/"),
        SLASHEQ("/="),
        STAR("*"),
        STAREQ("*="),
        STRINGLITERAL(TokenKind.STRING),
        SWITCH("switch"),
        TILDE("~"),
        TRUE("true"),
        VAR("var"),
        WHILE("while"),
        YIELD("yield");

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
        public final TokenKind kind;

        TokenType() {
            this(null, TokenKind.DEFAULT);
        }

        TokenType(TokenKind kind) {
            this(null, kind);
        }

        TokenType(String value) {
            this(value, TokenKind.DEFAULT);
        }

        TokenType(String value, TokenKind kind) {
            this.value = value;
            this.kind = kind;
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
