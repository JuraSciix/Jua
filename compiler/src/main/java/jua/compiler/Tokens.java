package jua.compiler;

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

        // EXPRESSION KEYWORDS
        NULL("null"),

        ARROW("->"),
        COL(":"),
        COMMA(","),
        CUSTOM,
        DOT("."),
        EOF,
        STRINGLITERAL(TokenKind.STRING),
        FLOATLITERAL(TokenKind.NUMERIC),
        IDENTIFIER(TokenKind.NAMED),
        INTLITERAL(TokenKind.NUMERIC),
        LBRACE("{"),
        LBRACKET("["),
        LPAREN("("),
        QUES("?"),
        RBRACE("}"),
        RBRACKET("]"),
        RPAREN(")"),
        SEMI(";"),
        INVALID;

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

        private static boolean checkRange(TokenType target, TokenType lo, TokenType hi) {
            int o = target.ordinal();
            return lo.ordinal() <= o && o <= hi.ordinal();
        }

        public static boolean isBinaryOperator(TokenType t) {
            return checkRange(t, PLUS, QUESQUES);
        }

        public static boolean isUnaryOperator(TokenType t) {
            return checkRange(t, BANG, AT) || t == PLUS || t == MINUS;
        }

        public static boolean isEnhancedAsgOperator(TokenType t) {
            return checkRange(t, PLUSEQ, QUESQUESEQ);
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
