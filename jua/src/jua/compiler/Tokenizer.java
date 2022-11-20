package jua.compiler;

import java.io.StringWriter;

import static java.lang.Character.*;
import static jua.compiler.Tokens.*;
import static jua.compiler.Tokens.TokenType.*;

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

        public Token buildNamed(TokenType type) {
            return new Token(type, pos);
        }

        public Token buildNamedOrString() {
            String s = buffer.toString();
            TokenType k = TokenType.lookupIdentifier(s);
            // identifier requires saving the name, StringToken allows this
            return new NamedToken(k, pos, s);
        }

        public Token buildString() {
            return new StringToken(STRINGLITERAL, pos, buffer.toString());
        }

        public Token buildNumber(boolean isFloat, int radix) {
            if (isFloat) {
                return new NumericToken(FLOATLITERAL, pos, buffer.toString(), 10);
            } else {
                return new NumericToken(INTLITERAL, pos, buffer.toString(), radix);
            }
        }
    }

    private final Source source;

    private final SourceReader reader;

    private final Log log;

    private Token eofToken;

    private int pos;

    public Tokenizer(Source source) {
        this.source = source;
        reader = SourceReader.of(source.content);
        log = source.getLog();
    }

    public Source getSource() {
        return source;
    }

    public boolean hasMoreTokens() {
        return true;
    }

    public Token nextToken() {
        while (reader.hasMore()) {
            pos = reader.cursor();
            char ch = reader.peekChar();

            switch (ch) {
                // ASCII whitespaces
                case '\r': case '\n': case '\t': case ' ':
                case '\u0000': case '\u0001': case '\u0002':
                case '\u0003': case '\u0004': case '\u0005':
                case '\u0006': case '\u0007': case '\u0008':
                case '\u000E': case '\u000F': case '\u0010':
                case '\u0011': case '\u0012': case '\u0013':
                case '\u0014': case '\u0015': case '\u0016':
                case '\u0017': case '\u0018': case '\u0019':
                case '\u001B': case '\u007F':
                    reader.readChar();
                    continue;

                // ASCII identifier start chars
                case 'a': case 'b': case 'c': case 'd':
                case 'e': case 'f': case 'g': case 'h':
                case 'i': case 'j': case 'k': case 'l':
                case 'm': case 'n': case 'o': case 'p':
                case 'q': case 'r': case 's': case 't':
                case 'u': case 'v': case 'w': case 'x':
                case 'y': case 'z':
                case 'A': case 'B': case 'C': case 'D':
                case 'E': case 'F': case 'G': case 'H':
                case 'I': case 'J': case 'K': case 'L':
                case 'M': case 'N': case 'O': case 'P':
                case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X':
                case 'Y': case 'Z':
                case '$': case '_': case '\u001A':
                    reader.readChar();
                    return parseKeyword(ch);

                case '0': case '1': case '2': case '3':
                case '4': case '5': case '6': case '7':
                case '8': case '9':
                    reader.readChar();
                    return parseNumber(ch);

                case '.':
                    reader.readChar();
                    if (reader.hasMore()) {
                        switch (reader.peekChar()) {
                            case '0': case '1': case '2': case '3':
                            case '4': case '5': case '6': case '7':
                            case '8': case '9':
                                return parseNumber('.');
                        }
                    }
                    return new Token(DOT, pos);

                case '#':
                    log.waring("Comments which starts with '#' are deprecated and will be removed in near future");
                    reader.readChar();
                    parseSingleLineComment();
                    continue;

                case '/':
                    reader.readChar();
                    if (reader.hasMore()) {
                        ch = reader.peekChar();
                        switch (ch) {
                            case '/':
                                reader.readChar();
                                parseSingleLineComment();
                                continue;
                            case '*':
                                reader.readChar();
                                parseMultiLineComment(pos);
                                continue;
                            case '=':
                                reader.readChar();
                                return new Token(SLASHEQ, pos);
                        }
                    }
                    return new Token(SLASH, pos);

                case '&': case '|': case '^':
                case '=': case '!': case '>':
                case '<': case '-': case '%':
                case '+': case '?': case '*':
                    reader.readChar();
                    return parseSpecial(ch);

                case ',': reader.readChar(); return new Token(COMMA, pos);
                case ';': reader.readChar(); return new Token(SEMI, pos);
                case ':': reader.readChar(); return new Token(COL, pos);
                case '~': reader.readChar(); return new Token(TILDE, pos);
                case '{': reader.readChar(); return new Token(LBRACE, pos);
                case '(': reader.readChar(); return new Token(LPAREN, pos);
                case '[': reader.readChar(); return new Token(LBRACKET, pos);
                case '}': reader.readChar(); return new Token(RBRACE, pos);
                case ')': reader.readChar(); return new Token(RPAREN, pos);
                case ']': reader.readChar(); return new Token(RBRACKET, pos);

                case '\'': reader.readChar(); return parseString('\'');
                case '\"': reader.readChar(); return parseString('\"');

                default:
                    if (Character.isHighSurrogate(ch) || ch > 0x7f) { // Все валидные ASCII обработаны выше.
                        int cp = reader.readCodePoint();

                        if (Character.isWhitespace(cp)) {
                            continue;
                        }

                        if (Character.isJavaIdentifierStart(cp)) {
                            return parseKeyword(cp);
                        }
                    } else {
                        reader.readChar();
                    }
                    log.error(pos, "Illegal character");
            }
        }

        if (eofToken == null) eofToken = new Token(EOF, reader.cursor() + 1);
        return eofToken;
    }

    private void parseSingleLineComment() {
        while (reader.hasMore() && reader.readChar() != '\n') ;
    }

    private void parseMultiLineComment(int pos) {
        while (reader.hasMore()) {
            char c1 = reader.readChar();
            if (c1 == '*' && reader.hasMore()) {
                char c2 = reader.readChar();
                if (c2 == '/') {
                    return;
                }
            }
        }
        log.error(pos, "Unterminated multi-line comment");
    }

    private Token parseString(int mark) {
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
            case 'b': builder.putChar('\b'); return;
            case 'f': builder.putChar('\f'); return;
            case 'n': builder.putChar('\n'); return;
            case 'r': builder.putChar('\r'); return;
            case 't': builder.putChar('\t'); return;
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
        int i = (c >= '4' ? 2 : 3);
        int oct = (c - '0');
        int next = reader.peekChar();

        while (--i >= 0 && next >= '0' && next <= '7') {
            oct = (oct << 3) + reader.readChar() - '0';
            next = reader.peekChar();
        }
        builder.putChar(oct);
    }

    private Token parseNumber(int c) {
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

    private Token parseHex(TokenBuilder builder) {
        reader.readChar();
        int next = reader.peekChar();

        if (digit(next, 16) < 0 && next != '_') {
            tError(builder.pos, "illegal hexadecimal literal.");
        }
        parseDigits(builder, 'x', 16);
        return builder.buildNumber(false, 16);
    }

    private Token parseBin(TokenBuilder builder) {
        reader.readChar();
        int next = reader.peekChar();

        if (digit(next, 2) < 0 && next != '_') {
            tError(builder.pos, "illegal binary decimal literal.");
        }
        parseDigits(builder, 'b', 10);
        return builder.buildNumber(false, 2);
    }

    private Token parseDuo(TokenBuilder builder) {
        reader.readChar();
        int next = reader.peekChar();

        if (digit(next, 12) < 0 && next != '_') {
            tError(builder.pos, "illegal duodecimal literal.");
        }
        parseDigits(builder, 'b', 12);
        return builder.buildNumber(false, 12);
    }

    private Token parseFraction(TokenBuilder builder, int radix, boolean isFloat) {
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

    private Token parseKeyword(int c) {
        TokenBuilder builder = getBuilder(c);

        while (isJavaIdentifierPart(reader.peekCodePoint())) {
            builder.putChar(reader.readCodePoint());
        }
        return builder.buildNamedOrString();
    }

    private Token parseSpecial(int c) {
        TokenBuilder builder = getBuilder(-1);
        TokenType type = null;
        // Обожаю костыли.
        // todo: Переписать лексер.
        boolean f = true;
        do {
            builder.putChar(c);
            TokenType lookup = TokenType.lookupNullable(builder.buffer.toString());

            if (lookup == null) {
                break;
            }
            type = lookup;
            if (!reader.hasMore()) break;
            if (f) {
                f = false;
            } else {
                reader.readChar();
            }
            c = reader.peekChar();
        } while (seenSpecial());

        return checkSpecial(builder, type);
    }

    private boolean seenSpecial() {
        if (!reader.hasMore()) return false;
        switch (reader.peekChar()) {
            case '&': case '|': case '^':
            case '=': case '!': case '>':
            case '<': case '-': case '%':
            case '+': case '?': case '*':
                return true;
            default:
                return false;
        }
    }

    private Token checkSpecial(TokenBuilder builder, TokenType type) {
        if (type == null) {
            tError(builder.pos, "illegal character.");
        }
        return builder.buildNamed(type);
    }

    private TokenBuilder getBuilder() {
        return getBuilder(-1);
    }

    private TokenBuilder getBuilder(int c) {
        TokenBuilder builder = new TokenBuilder(pos);

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
