package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;

public class TokenStream {

    private final Lexer lexer;

    private final Token[] queue = new Token[16];
    private int top = 0;
    private Token last;

    public TokenStream(Lexer lexer) {
        this.lexer = lexer;
    }

    public Token last() {
        return last;
    }

    public Token nextToken() {
        return take();
    }

    public Token take() {
        return last = (top == 0) ? lexer.nextToken() : queue[--top];
    }

    public Token peek(int step) {
        while (top <= step) {
            queue[top++] = lexer.nextToken();
        }
        return queue[step];
    }

    public boolean matches(TokenType type) {
        return peek(0).type == type;
    }

    public boolean sequence(TokenType t1, TokenType t2) {
        return peek(1).type == t2 && peek(0).type == t1;
    }
}
