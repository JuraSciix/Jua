package jua.compiler.parser;

import jua.compiler.ParseException;
import jua.util.BufferReader;
import jua.util.Source;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;

import static java.lang.Character.*;
import static jua.compiler.parser.Tokens.*;
import static jua.compiler.parser.Tokens.TokenKind.*;

public class Tokenizer implements Closeable {

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

    private final Source source;

    private final BufferReader reader;

    public Tokenizer(Source source) {
        this.source = source;
        reader = source.createReader();
    }

    public Source getSource() {
        return source;
    }

    public boolean hasMoreTokens() {
        return true;
    }

    public Tokens.Token nextToken() throws ParseException, IOException {
        while (true) {
            int c;

            if ((c = reader.readChar()) < 0)
                return new DummyToken(EOF, reader.position()+1);

            if (c == '#') {
                parseComment();
                continue;
            }
            if (c > ' ') return parseCharacter(c);
        }
    }

    private void parseComment() throws IOException {
        int c = reader.readChar();
        while (c != -1 && c != '\n') {
            c = reader.readChar();
        }
    }

    private Tokens.Token parseCharacter(int c) throws ParseException, IOException {
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

    private Tokens.Token parseString(int mark) throws ParseException, IOException {
        TokenBuilder builder = getBuilder();

        for (int c; (c = reader.readChar()) != mark; ) {
            if (c < 0) {
                tError(reader.position(), "EOF reached while parsing string.");
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

    private void parseEscape(TokenBuilder builder) throws IOException {
        int c = reader.readChar();

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

    private void parseEscapeOctal(TokenBuilder builder, int c) throws IOException {
        int i    = (c >= '4' ? 2 : 3);
        int oct  = (c - '0');
        int next = reader.peekChar();

        while (--i >= 0 && next >= '0' && next <= '7') {
            oct = (oct << 3) + reader.readChar() - '0';
            next = reader.peekChar();
        }
        builder.putChar(oct);
    }

    private Tokens.Token parseNumber(int c) throws ParseException, IOException {
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

    private Tokens.Token parseHex(TokenBuilder builder) throws ParseException, IOException {
        reader.readChar();
        int next = reader.peekChar();

        if (digit(next, 16) < 0 && next != '_') {
            tError(builder.pos, "illegal hexadecimal literal.");
        }
        parseDigits(builder, 'x', 16);
        return builder.buildNumber(false, 16);
    }

    private Tokens.Token parseBin(TokenBuilder builder) throws ParseException, IOException {
        reader.readChar();
        int next = reader.peekChar();

        if (digit(next, 2) < 0 && next != '_') {
            tError(builder.pos, "illegal binary decimal literal.");
        }
        parseDigits(builder, 'b', 10);
        return builder.buildNumber(false, 2);
    }

    private Tokens.Token parseDuo(TokenBuilder builder) throws ParseException, IOException {
        reader.readChar();
        int next = reader.peekChar();

        if (digit(next, 12) < 0 && next != '_') {
            tError(builder.pos, "illegal duodecimal literal.");
        }
        parseDigits(builder, 'b', 12);
        return builder.buildNumber(false, 12);
    }

    private Tokens.Token parseFraction(TokenBuilder builder, int radix, boolean isFloat) throws ParseException, IOException {
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

    private void parseDigits(TokenBuilder builder, int c, int radix) throws ParseException, IOException {
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

    private void underscore() throws ParseException, IOException {
        tError(reader.position(), "underscore is not allowed here.");
    }

    private Tokens.Token parseKeyword(int c) throws IOException {
        TokenBuilder builder = getBuilder(c);

        while (isJavaIdentifierPart(reader.peekChar())) {
            builder.putChar(reader.readChar());
        }
        return builder.buildNamedOrString();
    }

    private Tokens.Token parseSpecial(int c) throws ParseException, IOException {
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
            c = reader.peekChar();
            if (f) {
                f = false;
            } else {
                reader.readChar();
            }
        } while (seenSpecial());

        return checkSpecial(builder, type);
    }

    private boolean seenSpecial() throws IOException {
        switch (reader.peekChar()) {
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

    private TokenBuilder getBuilder() throws IOException {
        return getBuilder(-1);
    }

    private TokenBuilder getBuilder(int c) throws IOException {
        TokenBuilder builder = new TokenBuilder(reader.position());

        if (c >= 0) {
            builder.putChar(c);
        }
        return builder;
    }

    private void tError(int position, String message) throws ParseException {
        throw new ParseException(message, position);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
