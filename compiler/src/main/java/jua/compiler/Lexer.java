package jua.compiler;

import jua.runtime.utils.Assert;

import static java.lang.Character.digit;
import static jua.compiler.Tokens.*;
import static jua.compiler.Tokens.TokenType.*;

public class Lexer {

    private final Source source;

    private final Log log;

    private final SourceReader reader;

    private Token eofToken;

    private int pos;

    private final StringBuilder buffer = new StringBuilder();

    private int radix;

    private TokenType type = INVALID;

    public Lexer(Source source, Log log) {
        this.source = source;
        this.log = log;
        reader = new SourceReader(source.content, 0, source.content.length);
    }

    private void report(int pos, String message) {
        log.error(source, pos, message);
    }

    public Token nextToken() {
        loop:
        while (true) {
            pos = reader.pos();

            switch (reader.peek()) {
                case -1:
                    if (eofToken == null) {
                        eofToken = new Token(EOF, pos);
                    }
                    return eofToken;

                // ASCII whitespaces
                case '\u0000': case '\u0001': case '\u0002':
                case '\u0003': case '\u0004': case '\u0005':
                case '\u0006': case '\u0007': case '\u0008':
                case '\t':     case '\n':     case '\r':
                case '\u000E': case '\u000F': case '\u0010':
                case '\u0011': case '\u0012': case '\u0013':
                case '\u0014': case '\u0015': case '\u0016':
                case '\u0017': case '\u0018': case '\u0019':
                case '\u001B': case '\u0020': case '\u007F':
                    reader.next();
                    continue;

                // ASCII identifier starts
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
                    scanIdentifier();
                    break loop;

                case '0':
                    buffer.append('0');
                    reader.next();
                    switch (reader.peek()) {
                        case 'x':
                        case 'X':
                            buffer.append((char) reader.peek());
                            reader.next();
                            scanHexadecimal();
                            break loop;
                        case 'b':
                        case 'B':
                            buffer.append((char) reader.peek());
                            reader.next();
                            if (seenDigitOrUnderscore(2)) {
                                scanDigits(2);
                                if (reader.peek() == '.') {
                                    buffer.append('.');
                                    reader.next();
                                    if (seenDigit(2)) {
                                        scanDigits(2);
                                        type = FLOATLITERAL;
                                        radix = 2;
                                    } else {
                                        report(pos, "malformed binary number");
                                    }
                                } else {
                                    type = INTLITERAL;
                                    radix = 2;
                                }
                            } else {
                                report(pos, "a binary number must contain at least one binary digit");
                            }
                            break loop;
                        case '_': {
                            int savedPos = reader.pos();

                            do {
                                reader.next();
                            } while (reader.peek() == '_');

                            if (!seenDigit(10)) {
                                report(savedPos, "a sequence of digits cannot end with an underscore");
                            }
                            break;
                        }
                    }

                    scanDigits(8);
                    if (reader.peek() == '.') {
                        buffer.append('.');
                        reader.next();
                        scanFraction();
                    } else {
                        type = INTLITERAL;
                        radix = 8;
                    }
                    break loop;
                case '1': case '2': case '3':
                case '4': case '5': case '6':
                case '7': case '8': case '9':
                    scanNumeric();
                    break loop;

                case '.':
                    reader.next();
                    switch (reader.peek()) {
                        case '0': case '1': case '2': case '3':
                        case '4': case '5': case '6': case '7':
                        case '8': case '9':
                            buffer.append('.');
                            scanFraction();
                            break loop;
                    }
                    return new Token(DOT, pos);

                case '/':
                    reader.next();
                    switch (reader.peek()) {
                        case '/':
                            parseSingleLineComment();
                            continue;
                        case '*':
                            parseMultiLineComment();
                            continue;
                        case '=':
                            reader.next();
                            return new Token(SLASHEQ, pos);
                    }
                    return new Token(SLASH, pos);

                case '?':
                    reader.next();
                    switch (reader.peek()) {
                        case '.':
                            reader.next();
                            type = QUESDOT;
                            break loop;
                        case '[':
                            reader.next();
                            type = QUESLBRACKET;
                            break loop;
                        case '?':
                            reader.next();
                            if (reader.peek() == '=') {
                                reader.next();
                                type = QUESQUESEQ;
                            }  else {
                                type = QUESQUES;
                            }
                            break loop;
                    }
                    type = QUES;
                    break loop;

                case '&': case '|': case '^':
                case '=': case '!': case '>':
                case '<': case '-': case '%':
                case '+': case '*':
                    scanOperator();
                    break loop;

                case ',': reader.next(); type = COMMA;    break loop;
                case ';': reader.next(); type = SEMI;     break loop;
                case ':': reader.next(); type = COL;      break loop;
                case '~': reader.next(); type = TILDE;    break loop;
                case '{': reader.next(); type = LBRACE;   break loop;
                case '(': reader.next(); type = LPAREN;   break loop;
                case '[': reader.next(); type = LBRACKET; break loop;
                case '}': reader.next(); type = RBRACE;   break loop;
                case ')': reader.next(); type = RPAREN;   break loop;
                case ']': reader.next(); type = RBRACKET; break loop;

                case '\'':
                    parseSimpleString();
                    break loop;

                case '\"':
                    parseNormString();
                    break loop;
            }

            int c = reader.peek();

            if (c > 0x7f) { // Все валидные ASCII обработаны выше.
                if (Character.isWhitespace(c)) {
                    continue;
                }

                if (Character.isJavaIdentifierStart(c)) {
                    scanIdentifier();
                    break;
                }
            } else {
                report(pos, "illegal character");
                reader.next();
            }
        }

        Token token;
        switch (type.kind) {
            case DEFAULT: token = new Token(type, pos);                                  break;
            case STRING:  token = new StringToken(type, pos, buffer.toString());         break;
            case NUMERIC: token = new NumericToken(type, pos, buffer.toString(), radix); break;
            case NAMED:   token = new NamedToken(type, pos, buffer.toString());          break;
            default: throw new AssertionError(type);
        }

        type = INVALID;
        buffer.setLength(0);

        return token;
    }

    private void parseSingleLineComment() {
        while (reader.next()) {
            int c = reader.peek();
            if (c == '\n') {
                reader.next();
                return;
            }
        }
    }

    private void parseMultiLineComment() {
        while (reader.next()) {
            int c1 = reader.peek();
            if (c1 == '*' && reader.next()) {
                int c2 = reader.peek();
                if (c2 == '/') {
                    reader.next();
                    return;
                }
            }
        }
        report(reader.pos(), "unexpected EOF, unterminated multiline comment");
    }

    private void scanNumeric() {
        scanDigits(10);

        switch (reader.peek()) {
            case '.':
                buffer.append('.');
                reader.next();
                scanFraction();
                break;
            case 'e':
            case 'E':
                buffer.append((char) reader.peek());
                reader.next();
                scanExponent();
                break;
            default:
                type = INTLITERAL;
                radix = 10;
        }
    }

    private void scanFraction() {
        if (seenDigitOrUnderscore(10)) {
            scanDigits(10);
        }

        if (reader.peek() == 'e' || reader.peek() == 'E') {
            buffer.append((char) reader.peek());
            reader.next();
            scanExponent();
        } else {
            type = FLOATLITERAL;
            radix = 10;
        }
    }

    private void scanExponent() {
        if (reader.peek() == '+' || reader.peek() == '-') {
            buffer.append((char) reader.peek());
            reader.next();
        }

        if (seenDigitOrUnderscore(10)) {
            scanDigits(10);
            type = FLOATLITERAL;
            radix = 10;
            return;
        }

        report(pos, "malformed floating-point literal");
    }

    private void scanHexadecimal() {
        if (seenDigitOrUnderscore(16)) {
            scanDigits(16);

            switch (reader.peek()) {
                case '.':
                    buffer.append('.');
                    reader.next();
                    scanHexFraction();
                    break;
                case 'p':
                case 'P':
                    buffer.append((char) reader.peek());
                    reader.next();
                    scanHexExponent();
                    break;
                default:
                    type = INTLITERAL;
                    radix = 16;
            }
            return;
        }

        if (reader.peek() == '.') {
            buffer.append('.');
            reader.next();
            scanHexFraction();
            return;
        }

        report(pos, "a hexadecimal number must contain at least one hexadecimal digit");
    }

    private void scanHexFraction() {
        if (seenDigitOrUnderscore(16)) {
            scanDigits(16);
            if (reader.peek() == 'p' || reader.peek() == 'P') {
                buffer.append((char) reader.peek());
                reader.next();
                scanHexExponent();
                return;
            }
        }
        report(pos, "malformed hexadecimal literal");
    }

    private void scanHexExponent() {
        if (reader.peek() == '+' || reader.peek() == '-') {
            buffer.append((char) reader.peek());
            reader.next();
        }
        if (seenDigitOrUnderscore(10)) {
            scanDigits(10);
            type = FLOATLITERAL;
            radix = 16;
            return;
        }
        report(pos, "malformed hexadecimal literal");
    }

    private boolean seenDigitOrUnderscore(int radix) {
        int c = reader.peek();
        return (c >= 0) && (c == '_' || Character.digit(c, radix) >= 0);
    }

    private boolean seenDigit(int radix) {
        int c = reader.peek();
        return (c >= 0) && Character.digit(c, radix) >= 0;
    }

    private void scanDigits(int radix) {
        if (reader.peek() == '_') {
            report(reader.pos(), "a sequence of digits cannot start with an underscore");

            do {
                reader.next();
            } while (reader.peek() == '_');
        }

        int lastUnderscorePos = -1;

        int normRadix = Math.max(radix, 10);

        while (seenDigitOrUnderscore(normRadix)) {
            int c = reader.peek();

            if (c == '_') {
                if (lastUnderscorePos < 0) {
                    lastUnderscorePos = reader.pos();
                }
            } else {
                lastUnderscorePos = -1;
                buffer.appendCodePoint(c);
            }
            reader.next();
        }

        if (lastUnderscorePos >= 0) {
            report(lastUnderscorePos, "a sequence of digits cannot end with an underscore");
        }
    }

    private void parseSimpleString() {
        reader.next();

        loop: while (true) {
            switch (reader.peek()) {
                case -1:
                    report(pos, "unexpected EOF, unterminated string literal");
                    break loop;
                case '\'':
                    reader.next();
                    break loop;
                default:
                    buffer.appendCodePoint(reader.peek());
                    reader.next();
            }
        }

        type = STRINGLITERAL;
    }

    private void parseNormString() {
        reader.next();

        loop: while (true) {
            switch (reader.peek()) {
                case -1:
                    report(pos, "unexpected EOF, unterminated string literal");
                    break loop;
                case '\"':
                    reader.next();
                    break loop;
                case '\\':
                    reader.next();
                    parseEscape();
                    break;
                default:
                    buffer.appendCodePoint(reader.peek());
                    reader.next();
            }
        }

        type = STRINGLITERAL;
    }

    private void parseEscape() {
        switch (reader.peek()) {
            case 'b':  reader.next(); buffer.append('\b'); break;
            case 'f':  reader.next(); buffer.append('\f'); break;
            case 'n':  reader.next(); buffer.append('\n'); break;
            case 'r':  reader.next(); buffer.append('\r'); break;
            case 't':  reader.next(); buffer.append('\t'); break;
            case '\'': reader.next(); buffer.append('\''); break;
            case '\"': reader.next(); buffer.append('\"'); break;
            case '\\': reader.next(); buffer.append('\\'); break;
            case '0': case '1': case '2':
            case '3': case '4': case '5':
            case '6': case '7': {
                char o = (char) (reader.peek() - '0');
                reader.next();
                int c = reader.peek();
                if ('0' <= c && c <= '7') {
                    reader.next();
                    o <<= 3;
                    o |= c - '0';
                    c = reader.peek();
                    if ('0' <= c && c <= '7' && o <= 0x1F) {
                        reader.next();
                        o <<= 3;
                        o |= c - '0';
                    }
                }
                buffer.append(o);
                break;
            }
            case 'u':
            case 'U': {
                reader.next();
                char u = 0;
                for (int i = 0; i < 4; i++) {
                    int d = digit(reader.peek(), 16);

                    if (d < 0) {
                        report(reader.pos(), "a unicode point can only contain hexadecimal digits");
                        break;
                    }
                    u <<= 4;
                    u |= d;
                    reader.next();
                }
                buffer.append(u);
                break;
            }
            default:
                buffer.append('\\');
                buffer.appendCodePoint(reader.peek());
                reader.next();
        }
    }

    private void scanIdentifier() {
        buffer.appendCodePoint(reader.peek());

        while (reader.next()) {
            int codePoint = reader.peek();

            if (Character.isJavaIdentifierPart(codePoint)) {
                buffer.appendCodePoint(codePoint);
            } else {
                break;
            }
        }

        type = lookup(buffer.toString());
        if (type == null) {
            type = IDENTIFIER;
        }
    }

    private void scanOperator() {
        buffer.appendCodePoint(reader.peek());
        TokenType a = null;
        TokenType b = lookup(buffer.toString());

        while (b != null) {
            a = b;
            reader.next();
            buffer.appendCodePoint(reader.peek());
            b = lookup(buffer.toString());
        }

        Assert.checkNonNull(a, "token type not installed");
        type = a;
    }
}
