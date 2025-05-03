package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;

import java.util.List;

public class TokenStream {
    private final Token[] tokens;
    private int top = 0;

    public TokenStream(List<Token> lexer) {
        // Копируем токены из списка в массив ради независимости,
        // и чтобы не тянуть за собой лишний объект.
        this.tokens = new Token[lexer.size()];
        lexer.toArray(tokens);
    }

    public int remaining() {
        return tokens.length - top;
    }

    public Token last() {
        if (top <= 0)
            return null;
        return tokens[top - 1];
    }

    public Token nextToken() {
        return take();
    }

    public Token take() {
        if (remaining() <= 0)
            return null;
        return tokens[top++];
    }

    public Token peek(int step) {
        if (remaining() <= 0)
            return null;
        return tokens[top + step];
    }

    public boolean matches(TokenType type) {
        return remaining() >= 1 && peek(0).type == type;
    }

    public boolean sequence(TokenType t1, TokenType t2) {
        return remaining() >= 2 && peek(1).type == t2 && peek(0).type == t1;
    }
}
