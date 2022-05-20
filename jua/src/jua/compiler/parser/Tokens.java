package jua.compiler.parser;

import java.util.HashMap;

public final class Tokens {

    private Tokens() {
        throw new AssertionError();
    }

    public interface TokenVisitor {
        void visitOperator(OperatorToken token);
        void visitDummy(DummyToken token);
        void visitString(StringToken token);
        void visitNumeric(NumberToken token);
    }

    public abstract static class Token {

        public final TokenKind type;

        public final int pos;

        protected Token(TokenKind type, int pos) {
            this.type = type;
            this.pos = pos;
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

        public abstract void accept(TokenVisitor visitor);
    }

    public static class DummyToken extends Token {

        // used only for EOF
        public DummyToken(TokenKind type, int pos) {
            super(type, pos);
        }

        @Override
        public void accept(TokenVisitor visitor) {
            visitor.visitDummy(this);
        }
    }

    public static class OperatorToken extends Token {

        public OperatorToken(TokenKind type, int pos) {
            super(type, pos);
        }

        @Override
        public String toString() {
            return type.toString();
        }

        @Override
        public void accept(TokenVisitor visitor) {
            visitor.visitOperator(this);
        }
    }

    public static class StringToken extends Token {

        public final String value;

        public StringToken(TokenKind type, int pos, String value) {
            super(type, pos);
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

        @Override
        public void accept(TokenVisitor visitor) {
            visitor.visitString(this);
        }
    }

    public static class NumberToken extends StringToken {

        public final int radix;

        public NumberToken(TokenKind type, int position, String value, int radix) {
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

        @Override
        public void accept(TokenVisitor visitor) {
            visitor.visitNumeric(this);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public enum TokenKind {

        AMP("&"),
        AMPAMP("&&"),
        AMPEQ("&="),
        BAR("|"),
        BARBAR("||"),
        BAREQ("|="),
        BREAK("break"),
        CARET("^"),
        CARETEQ("^="),
        CASE("case"),
        CLONE("clone"),
        COLON(":"),
        COMMA(","),
        CONST("const"),
        CONTINUE("continue"),
        DEFAULT("default"),
        DO("do"),
        DOT("."),
        ELSE("else"),
        EOF,
        EQ("="),
        EQEQ("=="),
        EXCL("!"),
        EXLCEQ("!="),
        FALLTHROUGH("fallthrough"),
        FALSE("false"),
        FLOATLITERAL,
        FN("fn"),
        FOR("for"),
        GT(">"),
        GTEQ(">="),
        GTGT(">>"),
        GTGTEQ(">>="),
        IDENTIFIER,
        IF("if"),
        INTLITERAL,
        LBRACE("{"),
        LBRACKET("["),
        LPAREN("("),
        LT("<"),
        LTEQ("<="),
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
        //    PRINT("print"),
        //    PRINTLN("println"),
        QUES("?"),
        QUESQUES("??"),
        QUESQUESEQ("??="),
        RBRACE("}"),
        RBRACKET("]"),
        RETURN("return"),
        RPAREN(")"),
        SEMICOLON(";"),
        SLASH("/"),
        SLASHEQ("/="),
        STAR("*"),
        STAREQ("*="),
        STRINGLITERAL,
        SWITCH("switch"),
        TILDE("~"),
        TRUE("true"),
        WHILE("while");

        public final String name;

        TokenKind() {
            this(null);
        }

        TokenKind(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return (name != null) ? '\'' + name + '\'' : '<' + name() + '>';
        }
    }

    private static final HashMap<String, TokenKind> LOOKUP_KINDS;

    static {
        TokenKind[] kinds = TokenKind.values();
        HashMap<String, TokenKind> lookupKinds = new HashMap<>(kinds.length, 1.0f);

        for (TokenKind kind : kinds) {
            if (kind.name != null) {
                lookupKinds.put(kind.name, kind);
            }
        }

        LOOKUP_KINDS = lookupKinds;
    }

    public static TokenKind lookupKind(String name) {
        return LOOKUP_KINDS.get(name);
    }
}
