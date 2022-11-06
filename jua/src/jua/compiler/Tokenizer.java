package jua.compiler;

import java.io.StringWriter;

import static java.lang.Character.*;
import static jua.compiler.Tokens.*;
import static jua.compiler.Tokens.TokenKind.*;

public class Tokenizer implements AutoCloseable {

    private static class TokenBuilder {

        private final int pos;

        private final StringWriter buffer = new StringWriter();

        private TokenBuilder(int pos) {
            this.pos = pos;
        }

        public TokenBuilder putChar(int c) {
            buffer.write(c);
            return this;
        }

        public Tokens.Token buildNamed(Tokens.TokenKind type) {
            return new OperatorToken(type, pos);
        }

        public Tokens.Token buildNamedOrString() {
            String s = buffer.toString();
            TokenKind k = lookupKind(s);
            if (k == null) k = IDENTIFIER;
            // identifier requires saving the name, StringToken allows this
            return new StringToken(k, pos, s);
        }

        public Tokens.Token buildString() {
            return new StringToken(STRINGLITERAL, pos, buffer.toString());
        }

        public Tokens.Token buildNumber(boolean isFloat, int radix) {
            if (isFloat) {
                return new NumberToken(FLOATLITERAL, pos, buffer.toString(), 10);
            } else {
                return new NumberToken(INTLITERAL, pos, buffer.toString(), radix);
            }
        }
    }

    private final Source source;

    private final SourceReader reader;

    private final Log log;

    public Tokenizer(Source source) {
        this.source = source;
        reader = source.createReader();
        log = source.createLog();
    }

    public Source getSource() {
        return source;
    }

    public boolean hasMoreTokens() {
        return true;
    }

    public Tokens.Token nextToken() {
        while (true) {
            int c;

            if (!reader.hasMore()) return new DummyToken(EOF, reader.cursor() + 1);

            c = reader.readCodePoint();

            if (c == '#') {
                parseComment();
                continue;
            }
            if (!Character.isWhitespace(c)) return parseCharacter(c);
        }
    }

    private void parseComment() {
        int c;
        while (reader.hasMore() && (c = reader.readChar()) != '\n') {
            c = reader.readChar();
        }
    }

    private Tokens.Token parseCharacter(int c) {
        if (c == '\'' || c == '"') {
            return parseString(c);
        }
        if (isDigit(c) || c == '.' && isDigit(reader.peekChar())) {
            return parseNumber(c);
        }
        if (isJavaIdentifierStart(c)) {
            return parseKeyword(c);
        }
        return parseSpecial(c);
    }

    private Tokens.Token parseString(int mark) {
        TokenBuilder builder = getBuilder();
        int c = 0;

        while (reader.hasMore() && (c = reader.readChar()) != mark) {
            if (c == '\\') {
                parseEscape(builder);
            } else {
                builder.putChar(c);
            }
        }
        if (!reader.hasMore() && c != mark) {
            tError(reader.cursor(), "EOF reached while parsing string.");
        }
        return builder.buildString();
    }

    private void parseEscape(TokenBuilder builder) {
        int c = reader.readChar();

        switch (c) {
            case 'b':
                builder.putChar('\b');
                return;
            case 'f':
                builder.putChar('\f');
                return;
            case 'n':
                builder.putChar('\n');
                return;
            case 'r':
                builder.putChar('\r');
                return;
            case 't':
                builder.putChar('\t');
                return;
            case '\'':
                builder.putChar('\'');
                return;
            case '\"':
                builder.putChar('\"');
                return;
            case '\\':
                builder.putChar('\\');
                return;
        }
        if (c >= '0' && c <= '7') {
            parseEscapeOctal(builder, c);
            return;
        }
        builder.putChar('\\').putChar(c);
    }

    private void parseEscapeOctal(TokenBuilder builder, int c) {
        int i = (c >= '4' ? 2 : 3);
        int oct = (c - '0');
        int next = reader.peekChar();

        while (--i >= 0 && next >= '0' && next <= '7') {
            oct = (oct << 3) + reader.readChar() - '0';
            next = reader.peekChar();
        }
        builder.putChar(oct);
    }

    private Tokens.Token parseNumber(int c) {
        TokenBuilder builder = getBuilder();
        int next = reader.peekChar();
        int radix = 10;

        if (c == '0') {
            if (next == 'x' || next == 'X') return parseHex(builder);
            if (next == 'b' || next == 'B') return parseBin(builder);
            if (next == 'd' || next == 'D') return parseDuo(builder);
            radix = 8;
        }
        builder.putChar(c);
        parseDigits(builder, c, 10);
        return parseFraction(builder, radix, (c == '.'));
    }

    private Tokens.Token parseHex(TokenBuilder builder) {
        reader.readChar();
        int next = reader.peekChar();

        if (digit(next, 16) < 0 && next != '_') {
            tError(builder.pos, "illegal hexadecimal literal.");
        }
        parseDigits(builder, 'x', 16);
        return builder.buildNumber(false, 16);
    }

    private Tokens.Token parseBin(TokenBuilder builder) {
        reader.readChar();
        int next = reader.peekChar();

        if (digit(next, 2) < 0 && next != '_') {
            tError(builder.pos, "illegal binary decimal literal.");
        }
        parseDigits(builder, 'b', 10);
        return builder.buildNumber(false, 2);
    }

    private Tokens.Token parseDuo(TokenBuilder builder) {
        reader.readChar();
        int next = reader.peekChar();

        if (digit(next, 12) < 0 && next != '_') {
            tError(builder.pos, "illegal duodecimal literal.");
        }
        parseDigits(builder, 'b', 12);
        return builder.buildNumber(false, 12);
    }

    private Tokens.Token parseFraction(TokenBuilder builder, int radix, boolean isFloat) {
        int c;

        if ((c = reader.peekChar()) == '.') {
            if (isFloat) {
                return builder.buildNumber(true, 10);
            }
            builder.putChar(reader.readChar());
            parseDigits(builder, '.', 10);
            c = reader.peekChar();
            isFloat = true;
        }
        if (c == 'e' || c == 'E') {
            builder.putChar(reader.readChar());
            c = reader.peekChar();

            if (c == '-' || c == '+') {
                builder.putChar(reader.readChar());
                c = reader.peekChar();
            }
            if (!isDigit(c) && c != '_') {
                tError(builder.pos, "malformed floating literal.");
            }
            parseDigits(builder, c, 10);
            isFloat = true;
        }
        return builder.buildNumber(isFloat, radix);
    }

    private void parseDigits(TokenBuilder builder, int c, int radix) {
        int next = reader.peekChar();

        if (digit(c, radix) < 0 && next == '_') {
            reader.readChar();
            underscore();
        }
        while (digit(next, radix) >= 0 || next == '_') {
            if ((c = reader.readChar()) != '_') {
                builder.putChar(c);
            }
            next = reader.peekChar();
        }
        if (c == '_') underscore();
    }

    private void underscore() {
        tError(reader.cursor(), "underscore is not allowed here.");
    }

    private Tokens.Token parseKeyword(int c) {
        TokenBuilder builder = getBuilder(c);

        while (isJavaIdentifierPart(reader.peekCodePoint())) {
            builder.putChar(reader.readCodePoint());
        }
        return builder.buildNamedOrString();
    }

    private Tokens.Token parseSpecial(int c) {
        TokenBuilder builder = getBuilder(-1);
        TokenKind type = null;
        // Обожаю костыли.
        // todo: Переписать лексер.
        boolean f = true;
        do {
            builder.putChar(c);
            TokenKind lookup = lookupKind(builder.buffer.toString());

            if (lookup == null) {
                break;
            }
            type = lookup;
            if (!reader.hasMore()) break;
            c = reader.peekChar();
            if (f) {
                f = false;
            } else {
                reader.readChar();
            }
        } while (seenSpecial());

        return checkSpecial(builder, type);
    }

    private boolean seenSpecial() {
        if (!reader.hasMore()) return false;
        switch (reader.peekChar()) {
            case '&':
            case '|':
            case '^':
            case ':':
            case ',':
            case '.':
            case '=':
            case '!':
            case '>':
            case '{':
            case '[':
            case '(':
            case '<':
            case '-':
            case '%':
            case '+':
            case '?':
            case '}':
            case ']':
            case ')':
            case ';':
            case '*':
            case '~':
                return true;
            default:
                return false;
        }
    }

    private Tokens.Token checkSpecial(TokenBuilder builder, Tokens.TokenKind type) {
        if (type == null) {
            tError(builder.pos, "illegal character.");
        }
        return builder.buildNamed(type);
    }

    private TokenBuilder getBuilder() {
        return getBuilder(-1);
    }

    private TokenBuilder getBuilder(int c) {
        TokenBuilder builder = new TokenBuilder(reader.cursor());

        if (c >= 0) {
            builder.putChar(c);
        }
        return builder;
    }

    private void tError(int position, String message) {
        log.error(position, message);
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }
}
