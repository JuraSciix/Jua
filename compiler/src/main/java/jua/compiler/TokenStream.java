package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;

public class TokenStream {

    private final Lexer lexer;

    private Token[] queue = new Token[16];
    private int top = 0;

    public TokenStream(Lexer lexer) {
        this.lexer = lexer;
    }

    public Token nextToken() {
        return take();
    }

    public Token take() {
        if (top == 0) {
            return lexer.nextToken();
        }
        return queue[--top];
    }

    public void take(int count) {
        for (int i = 0; i < count; i++) {
            take();
        }
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
