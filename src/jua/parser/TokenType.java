package jua.parser;

@SuppressWarnings("SpellCheckingInspection")
public enum TokenType {

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

    final String value;

    TokenType() {
        this(null);
    }

    TokenType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return (value != null) ? '\'' + value + '\'' : '<' + name() + '>';
    }
}
