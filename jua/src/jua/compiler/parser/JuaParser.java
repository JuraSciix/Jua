package jua.compiler.parser;

import jua.compiler.ParseException;
import jua.compiler.Tree.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jua.compiler.parser.Tokens.*;

import static jua.compiler.parser.Tokens.TokenKind.*;

public class JuaParser {

    private final Tokenizer tokenizer;

    private Tokens.Token currentToken;

    public JuaParser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Statement parse() throws ParseException {
        List<Statement> statements = new ArrayList<>();
        int startPos = 0;
        while (tokenizer.hasMoreTokens()) {
            if (currentToken == null) {
                next();
                startPos = currentToken.pos;
            }
            statements.add(parseStatement());
        }
        return new BlockStatement(startPos, statements);
    }

    private Statement parseStatement() throws ParseException {
        int position = currentToken.pos;

        if (match(BREAK)) {
            return parseBreak(position);
        }
        if (match(CASE)) {
            pError(position, "'case' is not allowed outside of switch.");
        }
        if (match(CONST)) {
            return parseConst(position);
        }
        if (match(CONTINUE)) {
            return parseContinue(position);
        }
        if (match(DEFAULT)) {
            pError(position, "'default' is not allowed outside of switch.");
        }
        if (match(DO)) {
            return parseDo(position);
        }
        if (match(ELSE)) {
            pError(position, "'else' is not allowed without if.");
        }
        if (match(EOF)) {
            pError(position, "missing expected statement.");
        }
        if (match(FALLTHROUGH)) {
            return parseFallthrough(position);
        }
        if (match(FN)) {
            return parseFunction(position);
        }
        if (match(FOR)) {
            return parseFor(position);
        }
        if (match(IF)) {
            return parseIf(position);
        }
        if (match(LBRACE)) {
            return parseBlock(position);
        }
//        if (match(PRINT)) {
//            return parsePrint(position);
//        }
//        if (match(PRINTLN)) {
//            return parsePrintln(position);
//        }
        if (match(RETURN)) {
            return parseReturn(position);
        }
        if (match(SEMICOLON)) {
            return Statement.EMPTY;
        }
        if (match(SWITCH)) {
            return parseSwitch(position);
        }
        if (match(WHILE)) {
            return new WhileStatement(position, parseExpression(), parseStatement());
        }
        return parseUnusedExpression();
    }

    private Statement parseBreak(int position) throws ParseException {
        expect(SEMICOLON);
        return new BreakStatement(position);
    }

    private Statement parseConst(int position) throws ParseException {
        List<String> names = new ArrayList<>();
        List<Expression> expressions = new ArrayList<>();

        do {
            Tokens.Token name = currentToken;
            expect(IDENTIFIER, EQ);
            names.add(name.getString());
            expressions.add(parseExpression());
        } while (match(COMMA));

        expect(SEMICOLON);
        return new ConstantDeclareStatement(position, names, expressions);
    }

    private Statement parseContinue(int position) throws ParseException {
        expect(SEMICOLON);
        return new ContinueStatement(position);
    }

    private Statement parseDo(int position) throws ParseException {
        Statement body = parseStatement();
        expect(WHILE);
        Expression cond = parseExpression();
        expect(SEMICOLON);
        return new DoStatement(position, body, cond);
    }

    private Statement parseFallthrough(int position) throws ParseException {
        expect(SEMICOLON);
        return new FallthroughStatement(position);
    }

    private Statement parseFunction(int position) throws ParseException {
        Tokens.Token name = currentToken;
        expect(IDENTIFIER, LPAREN);
        List<String> names = new ArrayList<>();
        List<Expression> optionals = new ArrayList<>();
        boolean comma = false;

        while (!match(RPAREN)) {
            if (match(EOF) || comma && !match(COMMA)) {
                expect(RPAREN);
            }
            Tokens.Token name0 = currentToken;
            expect(IDENTIFIER);
            names.add(name0.getString());

            if (match(EQ)) {
                optionals.add(parseExpression());
            } else if (!optionals.isEmpty()) {
                pError(name0.pos, "here must be a optional argument.");
            }
            comma = !match(COMMA);
        }
        return new FunctionDefineStatement(position, name.getString(), names, optionals, parseBody());
    }

    private Statement parseBody() throws ParseException {
        int position = currentToken.pos;
        if (match(LBRACE)) return parseBlock(position);
        if (match(EQ)) {
            Expression expr = parseExpression();
            expect(SEMICOLON);
            return expr;
        }
        expect(LBRACE);
        return null;
    }

    private Statement parseFor(int position) throws ParseException {
        boolean parens = match(LPAREN);
        List<Expression> init = null;

        if (!match(SEMICOLON)) {
            init = parseExpressions();
            expect(SEMICOLON);
        }
        Expression cond = null;

        if (!match(SEMICOLON)) {
            cond = parseExpression();
            expect(SEMICOLON);
        }
        List<Expression> step = null;

        if (parens) {
            if (!match(RPAREN)) {
                step = parseExpressions();
                expect(RPAREN);
            }
        } else {
            step = parseExpressions();
        }
        return new ForStatement(position, init, cond, step, parseStatement());
    }

    private Statement parseIf(int position) throws ParseException {

        Expression cond = parseExpression();
        Statement body = parseStatement();

        if (!match(ELSE)) {
            return new IfStatement(position, cond, body);
        }
        return new IfStatement(position, cond, body, parseStatement());
    }

    private Statement parseBlock(int position) throws ParseException {
        List<Statement> statements = new ArrayList<>();

        while (!match(RBRACE)) {
            if (match(EOF)) {
                expect(RBRACE);
            }
            statements.add(parseStatement());
        }
        return new BlockStatement(position, statements);
    }

//    private Statement parsePrint(int position) throws ParseException {
//        List<Expression> expressions = parseExpressions();
//        expect(SEMICOLON);
//        return new PrintStatement(position, expressions);
//    }
//
//    private Statement parsePrintln(int position) throws ParseException {
//        List<Expression> expressions = parseExpressions();
//        expect(SEMICOLON);
//        return new PrintlnStatement(position, expressions);
//    }

    private Statement parseReturn(int position) throws ParseException {
        if (match(SEMICOLON)) {
            return new ReturnStatement(position);
        }
        Expression expr = parseExpression();
        expect(SEMICOLON);
        return new ReturnStatement(position, expr);
    }

    private Statement parseSwitch(int position) throws ParseException {
        Expression selector = parseExpression();
        List<CaseStatement> cases = new ArrayList<>();
        expect(LBRACE);

        while (!match(RBRACE)) {
            int position1 = currentToken.pos;

            if (match(CASE)) {
                cases.add(parseCase(position1, false));
            } else if (match(DEFAULT)) {
                cases.add(parseCase(position1, true));
            } else if (match(EOF)) {
                expect(RBRACE);
            } else {
                pError(currentToken.pos, currentToken + " is not allowed inside switch.");
            }
        }
        return new SwitchStatement(position, selector, cases);
    }

    private CaseStatement parseCase(int position, boolean isDefault) throws ParseException {
        List<Expression> expressions = null;

        if (!isDefault) {
            expressions = parseExpressions();
        }
        expect(COLON);
        return new CaseStatement(position, expressions, parseStatement());
    }

    private Statement parseUnusedExpression() throws ParseException {
        int position = currentToken.pos;
        Expression expr = parseExpression();
        expect(SEMICOLON);
        return new DiscardedExpression(position, expr);
    }

    private List<Expression> parseExpressions() throws ParseException {
        List<Expression> expressions = new ArrayList<>();

        do {
            expressions.add(parseExpression());
        } while (match(COMMA));

        return expressions;
    }

    private Expression parseExpression() throws ParseException {
        return parseAssignment();
    }

    private Expression parseAssignment() throws ParseException {
        Expression expr = parseNullCoalesce();
        int position = currentToken.pos;

        if (match(AMPEQ)) {
            return new AssignBitAndExpression(position, expr, parseAssignment());
        }
        if (match(BAREQ)) {
            return new AssignBitOrExpression(position, expr, parseAssignment());
        }
        if (match(CARETEQ)) {
            return new AssignBitXorExpression(position, expr, parseAssignment());
        }
        if (match(EQ)) {
            return new AssignExpression(position, expr, parseAssignment());
        }
        if (match(GTGTEQ)) {
            return new AssignShiftLeftExpression(position, expr, parseAssignment());
        }
        if (match(LTLTEQ)) {
            return new AssignShiftRightExpression(position, expr, parseAssignment());
        }
        if (match(MINUSEQ)) {
            return new AssignSubtractExpression(position, expr, parseAssignment());
        }
        if (match(PERCENTEQ)) {
            return new AssignRemainderExpression(position, expr, parseAssignment());
        }
        if (match(PLUSEQ)) {
            return new AssignAddExpression(position, expr, parseAssignment());
        }
        if (match(QUESQUESEQ)) {
            return new AssignNullCoalesceExpression(position, expr, parseAssignment());
        }
        if (match(SLASHEQ)) {
            return new AssignDivideExpression(position, expr, parseAssignment());
        }
        if (match(STAREQ)) {
            return new AssignMultiplyExpression(position, expr, parseAssignment());
        }
        return expr;
    }

    private Expression parseNullCoalesce() throws ParseException {
        Expression expr = parseTernary();

        while (true) {
            int position = currentToken.pos;

            if (match(QUESQUES)) {
                expr = new NullCoalesceExpression(position, expr, parseTernary());
            } else {
                return expr;
            }
        }
    }

    private Expression parseTernary() throws ParseException {
        Expression expr = parseOr();

        while (true) {
            int position = currentToken.pos;

            if (match(QUES)) {
                expr = parseTernary0(position, expr);
            } else {
                return expr;
            }
        }
    }

    private Expression parseTernary0(int position, Expression cond) throws ParseException {
        Expression right = parseExpression();
        expect(COLON);
        return new TernaryExpression(position, cond, right, parseExpression());
    }

    private Expression parseOr() throws ParseException {
        Expression expr = parseAnd();
        int position = currentToken.pos;

        while (match(BARBAR)) {
            expr = new OrExpression(position, expr, parseAnd());
            position = currentToken.pos;
        }
        return expr;
    }

    private Expression parseAnd() throws ParseException {
        Expression expr = parseBitOr();

        while (true) {
            int position = currentToken.pos;

            if (match(AMPAMP)) {
                expr = new AndExpression(position, expr, parseEquality());
            } else {
                return expr;
            }
        }
    }


    private Expression parseBitOr() throws ParseException {
        Expression expr = parseBitXor();

        while (true) {
            int position = currentToken.pos;

            if (match(BAR)) {
                expr = new BitOrExpression(position, expr, parseBitXor());
            } else {
                return expr;
            }
        }
    }

    private Expression parseBitXor() throws ParseException {
        Expression expr = parseBitAnd();

        while (true) {
            int position = currentToken.pos;

            if (match(CARET)) {
                expr = new BitXorExpression(position, expr, parseBitAnd());
            } else {
                return expr;
            }
        }
    }

    private Expression parseBitAnd() throws ParseException {
        Expression expr = parseEquality();

        while (true) {
            int position = currentToken.pos;

            if (match(AMP)) {
                expr = new BitAndExpression(position, expr, parseEquality());
            } else {
                return expr;
            }
        }
    }

    private Expression parseEquality() throws ParseException {
        Expression expr = parseConditional();

        while (true) {
            int position = currentToken.pos;

            if (match(EQEQ)) {
                expr = new EqualExpression(position, expr, parseConditional());
            } else if (match(EXLCEQ)) {
                expr = new NotEqualExpression(position, expr, parseConditional());
            } else {
                return expr;
            }
        }
    }

    private Expression parseConditional() throws ParseException {
        Expression expr = parseShift();

        while (true) {
            int position = currentToken.pos;

            if (match(GT)) {
                expr = new GreaterExpression(position, expr, parseShift());
            } else if (match(GTEQ)) {
                expr = new GreaterEqualExpression(position, expr, parseShift());
            } else if (match(LT)) {
                expr = new LessExpression(position, expr, parseShift());
            } else if (match(LTEQ)) {
                expr = new LessEqualExpression(position, expr, parseShift());
            } else {
                return expr;
            }
        }
    }

    private Expression parseShift() throws ParseException {
        Expression expr = parseAdditive();

        while (true) {
            int position = currentToken.pos;

            if (match(GTGT)) {
                expr = new ShiftRightExpression(position, expr, parseAdditive());
            } else if (match(LTLT)) {
                expr = new ShiftLeftExpression(position, expr, parseAdditive());
            } else {
                return expr;
            }
        }
    }

    private Expression parseAdditive() throws ParseException {
        Expression expr = parseMultiplicative();

        while (true) {
            int position = currentToken.pos;

            if (match(MINUS)) {
                expr = new SubtractExpression(position, expr, parseMultiplicative());
            } else if (match(PLUS)) {
                expr = new AddExpression(position, expr, parseMultiplicative());
            } else {
                return expr;
            }
        }
    }

    private Expression parseMultiplicative() throws ParseException {
        Expression expr = parseUnary();

        while (true) {
            int position = currentToken.pos;

            if (match(PERCENT)) {
                expr = new RemainderExpression(position, expr, parseUnary());
            } else if (match(SLASH)) {
                expr = new DivideExpression(position, expr, parseUnary());
            } else if (match(STAR)) {
                expr = new MultiplyExpression(position, expr, parseUnary());
            } else {
                return expr;
            }
        }
    }

    private Expression parseUnary() throws ParseException {
        int position = currentToken.pos;

        if (match(EXCL)) {
            return new NotExpression(position, parseUnary());
        }
        if (match(MINUS)) {
            return new NegativeExpression(position, parseUnary());
        }
        if (match(MINUSMINUS)) {
            return new PreDecrementExpression(position, parseUnary());
        }
        if (match(PLUS)) {
            return new PositiveExpression(position, parseUnary());
        }
        if (match(PLUSPLUS)) {
            return new PreIncrementExpression(position, parseUnary());
        }
        if (match(TILDE)) {
            return new BitNotExpression(position, parseUnary());
        }
        return parseClone();
    }

    private Expression parseClone() throws ParseException {
        int position = currentToken.pos;

        if (match(CLONE)) {
            return new CloneExpression(position, parsePost());
        }
        return parsePost();
    }

    private Expression parsePost() throws ParseException {
        Expression expr = parseAccess();

        while (true) {
            int position = currentToken.pos;

            if (match(MINUSMINUS)) {
                expr = new PostDecrementExpression(position, expr);
            } else if (match(PLUSPLUS)) {
                expr = new PostIncrementExpression(position, expr);
            } else {
                return expr;
            }
        }
    }

    private Expression parseAccess() throws ParseException {
//        Expression expr = parsePrimary();
//        int position = currentToken.position;
//        int i = match(DOT) ? 1 : match(LBRACKET) ? 0 : -1;
//        return (i >= 0 ? parseArrayAccess(position, expr, i) : expr);

        Expression expression = parsePrimary();

        while (true) {
            int position = currentToken.pos;

            if (match(DOT)) {
                Tokens.Token token = currentToken;
                expect(IDENTIFIER);
                expression = new ArrayAccessExpression(position,
                        expression, new StringExpression(token.pos, token.getString()));
            } else if (match(LBRACKET)) {
                expression = new ArrayAccessExpression(position,
                        expression, parseExpression());
                expect(RBRACKET);
            } else {
                break;
            }
        }
        return expression;
    }

//    private Expression parseArrayAccess(int position, Expression expr, int dot)
//            throws ParseException {
//        List<Expression> keys = new ArrayList<>();
//
//        while (true) {
//            if ((dot == 1) || match(DOT)) {
//                Token key = currentToken;
//                expect(IDENTIFIER);
//                keys.add(new StringExpression(currentToken.position, key.getString()));
//                dot = 2;
//            } else if ((dot == 0) || match(LBRACKET)) {
//                keys.add(parseExpression());
//                expect(RBRACKET);
//                dot = 2;
//            } else {
//                return new ArrayAccessExpression(position, expr, keys);
//            }
//        }
//    }

    private Expression parsePrimary() throws ParseException {
        Tokens.Token token = currentToken;

        if (match(EOF)) {
            pError(token.pos, "missing expected expression.");
        }
        if (match(FALSE)) {
            return new FalseExpression(token.pos);
        }
        if (match(FLOATLITERAL)) {
            return parseFloat(token);
        }
        if (match(IDENTIFIER)) {
            return parseIdentifier(token);
        }
        if (match(INTLITERAL)) {
            return parseInt(token);
        }
        if (match(LBRACE)) {
            return parseArray(token.pos, RBRACE);
        }
        if (match(LBRACKET)) {
            return parseArray(token.pos, RBRACKET);
        }
        if (match(LPAREN)) {
            return parseParens(token.pos);
        }
        if (match(NULL)) {
            return new NullExpression(token.pos);
        }
        if (match(STRINGLITERAL)) {
            return new StringExpression(token.pos, token.getString());
        }
        if (match(TRUE)) {
            return new TrueExpression(token.pos);
        }
        unexpected(currentToken);
        return null;
    }

    private Expression parseFloat(Tokens.Token token) throws ParseException {
        double d = token.getDouble();

        if (Double.isInfinite(d)) {
            pError(token.pos, "number too large.");
        }
        if ((d == 0.0) && !token.getString().matches("\\.?0\\.?\\d*(?:[Ee][+-]\\d+)?$")) {
            pError(token.pos, "number too small.");
        }
        return new FloatExpression(token.pos, d);
    }

    private Expression parseIdentifier(Tokens.Token token) throws ParseException {
        if (match(LPAREN)) {
            return parseInvocation(token);
        }
        return new VariableExpression(token.pos, token.getString());
    }

    private Expression parseInvocation(Tokens.Token token) throws ParseException {
        List<Expression> args = new ArrayList<>();
        boolean comma = false;

        while (!match(RPAREN)) {
            if (match(EOF) || comma && !match(COMMA)) {
                expect(RPAREN);
            }
            args.add(parseExpression());
            comma = !match(COMMA);
        }
        return new FunctionCallExpression(token.pos, token.getString(), args);
    }

    private Expression parseInt(Tokens.Token token) throws ParseException {
        try {
            return new IntExpression(token.pos, token.getLong());
        } catch (NumberFormatException e) {
            pError(token.pos, "number too large.");
            return null;
        }
    }

    private Expression parseArray(int position, Tokens.TokenKind enclosing) throws ParseException {
        Map<Expression, Expression> map = new LinkedHashMap<>();
        boolean comma = false;

        while (!match(enclosing)) {
            if (match(EOF) || comma && !match(COMMA)) {
                expect(enclosing);
            }
            Expression key;
            Expression value = parseExpression();

            if (match(COLON)) {
                key = value;
                value = parseExpression();
            } else {
                key = Expression.empty();
            }
            map.put(key, value);
            comma = !match(COMMA);
        }
        return new ArrayExpression(position, map);
    }

    private Expression parseParens(int position) throws ParseException {
        Expression expr = parseExpression();
        expect(RPAREN);
        return new ParensExpression(position, expr);
    }

    private void next() throws ParseException {
        currentToken = tokenizer.nextToken();
    }

    private boolean match(Tokens.TokenKind type) throws ParseException {
        if (currentToken.type == type) {
            next();
            return true;
        }
        return false;
    }

    private void expect(Tokens.TokenKind... types) throws ParseException {
        for (Tokens.TokenKind type : types) {
            if (match(type)) {
                continue;
            }
            //next();

            if (type.name == null) {
                if (type == IDENTIFIER) {
                    pError(currentToken.pos, "identifier expected.");
                } else {
                    unexpected(currentToken);
                }
                return;
            }
            if (currentToken instanceof DummyToken) {
                pError(currentToken.pos, type + " expected.");
                return;
            }
            pError(currentToken.pos, type + " expected, " + currentToken + " found.");
        }
    }

    private void unexpected(Tokens.Token token) throws ParseException {
        pError(token.pos, "unexpected " + token + '.');
    }

    private void pError(int position, String message) throws ParseException {
        throw new ParseException(message, position);
    }
}
