package jua.compiler;

import java.io.StringWriter;

import static java.lang.Character.*;
import static jua.compiler.Tokens.*;
import static jua.compiler.Tokens.TokenKind.*;

public class Tokenizer {

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
            TokenKind k  = lookupKind(s);
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

    private final TokenizeStream stream;

    public Tokenizer(TokenizeStream stream) {
        this.stream = stream;
    }

    public String getFilename() {
        return stream.filename();
    }

    public boolean hasMoreTokens() {
        return stream.peek() >= 0;
    }

    public Tokens.Token nextToken() throws ParseException {
        while (true) {
            int c;

            if ((c = stream.next()) < 0)
                return new DummyToken(EOF, stream.getPosition()+1);

            if (c == '#') {
                parseComment();
                continue;
            }
            if (c > ' ') return parseCharacter(c);
        }
    }

    private void parseComment() {
        while (stream.next() != '\n') {
            if (stream.peek() < 0) {
                stream.nextLine(); // for correctly printing positions of errors
                return;
            }
        }
    }

    private Tokens.Token parseCharacter(int c) throws ParseException {
        if (c == '\'' || c == '"') {
            return parseString(c);
        }
        if (isDigit(c) || c == '.' && isDigit(stream.peek())) {
            return parseNumber(c);
        }
        if (isJavaIdentifierStart(c)) {
            return parseKeyword(c);
        }
        return parseSpecial(c);
    }

    private Tokens.Token parseString(int mark) throws ParseException {
        TokenBuilder builder = getBuilder();

        for (int c; (c = stream.next()) != mark; ) {
            if (c < 0) {
                tError(stream.getPosition(), "EOF reached while parsing string.");
                break;
            }
            if (c == '\\') {
                parseEscape(builder);
            } else {
                builder.putChar(c);
            }
        }
        return builder.buildString();
    }

    private void parseEscape(TokenBuilder builder) {
        int c = stream.next();

        switch (c) {
            case 'b':  builder.putChar('\b'); return;
            case 'f':  builder.putChar('\f'); return;
            case 'n':  builder.putChar('\n'); return;
            case 'r':  builder.putChar('\r'); return;
            case 't':  builder.putChar('\t'); return;
            case '\'': builder.putChar('\''); return;
            case '\"': builder.putChar('\"'); return;
            case '\\': builder.putChar('\\'); return;
        }
        if (c >= '0' && c <= '7') {
            parseEscapeOctal(builder, c);
            return;
        }
        builder.putChar('\\').putChar(c);
    }

    private void parseEscapeOctal(TokenBuilder builder, int c) {
        int i    = (c >= '4' ? 2 : 3);
        int oct  = (c - '0');
        int next = stream.peek();

        while (--i >= 0 && next >= '0' && next <= '7') {
            oct = (oct << 3) + stream.next() - '0';
            next = stream.peek();
        }
        builder.putChar(oct);
    }

    private Tokens.Token parseNumber(int c) throws ParseException {
        TokenBuilder builder = getBuilder();
        int next = stream.peek();
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

    private Tokens.Token parseHex(TokenBuilder builder) throws ParseException {
        stream.next();
        int next = stream.peek();

        if (digit(next, 16) < 0 && next != '_') {
            tError(builder.pos, "illegal hexadecimal literal.");
        }
        parseDigits(builder, 'x', 16);
        return builder.buildNumber(false, 16);
    }

    private Tokens.Token parseBin(TokenBuilder builder) throws ParseException {
        stream.next();
        int next = stream.peek();

        if (digit(next, 2) < 0 && next != '_') {
            tError(builder.pos, "illegal binary decimal literal.");
        }
        parseDigits(builder, 'b', 10);
        return builder.buildNumber(false, 2);
    }

    private Tokens.Token parseDuo(TokenBuilder builder) throws ParseException {
        stream.next();
        int next = stream.peek();

        if (digit(next, 12) < 0 && next != '_') {
            tError(builder.pos, "illegal duodecimal literal.");
        }
        parseDigits(builder, 'b', 12);
        return builder.buildNumber(false, 12);
    }

    private Tokens.Token parseFraction(TokenBuilder builder, int radix, boolean isFloat) throws ParseException {
        int c;

        if ((c = stream.peek()) == '.') {
            if (isFloat) {
                return builder.buildNumber(true, 10);
            }
            builder.putChar(stream.next());
            parseDigits(builder, '.', 10);
            c = stream.peek();
            isFloat = true;
        }
        if (c == 'e' || c == 'E') {
            builder.putChar(stream.next());
            c = stream.peek();

            if (c == '-' || c == '+') {
                builder.putChar(stream.next());
                c = stream.peek();
            }
            if (!isDigit(c) && c != '_') {
                tError(builder.pos, "malformed floating literal.");
            }
            parseDigits(builder, c, 10);
            isFloat = true;
        }
        return builder.buildNumber(isFloat, radix);
    }

    private void parseDigits(TokenBuilder builder, int c, int radix) throws ParseException {
        int next = stream.peek();

        if (digit(c, radix) < 0 && next == '_') {
            stream.next();
            underscore();
        }
        while (digit(next, radix) >= 0 || next == '_') {
            if ((c = stream.next()) != '_') {
                builder.putChar(c);
            }
            next = stream.peek();
        }
        if (c == '_') underscore();
    }

    private void underscore() throws ParseException {
        tError(stream.getPosition(), "underscore is not allowed here.");
    }

    private Tokens.Token parseKeyword(int c) {
        TokenBuilder builder = getBuilder(c);

        while (isJavaIdentifierPart(stream.peek())) {
            builder.putChar(stream.next());
        }
        return builder.buildNamedOrString();
    }

    private Tokens.Token parseSpecial(int c) throws ParseException {
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
            c = stream.peek();
            if (f) {
                f = false;
            } else {
                stream.next();
            }
        } while (seenSpecial());

        return checkSpecial(builder, type);
    }

    private boolean seenSpecial() {
        switch (stream.peek()) {
            case '&': case '|': case '^': case ':': case ',':
            case '.': case '=': case '!': case '>': case '{':
            case '[': case '(': case '<': case '-': case '%':
            case '+': case '?': case '}': case ']': case ')':
            case ';': case '*': case '~':
                return true;
            default:
                return false;
        }
    }

    private Tokens.Token checkSpecial(TokenBuilder builder, Tokens.TokenKind type) throws ParseException {
        if (type == null) {
            tError(builder.pos, "illegal character.");
        } else if (false && !type.name.equals(builder.buffer.toString())) {
            tError(builder.pos, "invalid token.");
        }
        return builder.buildNamed(type);
    }

    private TokenBuilder getBuilder() {
        return getBuilder(-1);
    }

    private TokenBuilder getBuilder(int c) {
        TokenBuilder builder = new TokenBuilder(stream.getPosition());

        if (c >= 0) {
            builder.putChar(c);
        }
        return builder;
    }

    private void tError(int position, String message) throws ParseException {
        throw new ParseException(message, position);
    }
}
