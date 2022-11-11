package jua.compiler;

import jua.compiler.Tokens.*;
import jua.compiler.Tree.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static jua.compiler.Tokens.TokenType.*;

public class JuaParser {

    // todo: Рефакторинг и оптимизация.

    private final Tokenizer tokenizer;

    private final Types types;

    private Token currentToken;

    private final Log log;

    public JuaParser(Tokenizer tokenizer, Types types, Log log) {
        this.tokenizer = Objects.requireNonNull(tokenizer, "Tokenizer is null");
        this.types = Objects.requireNonNull(types, "Types is null");
        this.log = log;
    }

    public Tree parse() {
        List<Statement> stats = new LinkedList<>();
        next();
        while (!match(EOF)) {
            stats.add(parseStatement());
        }
        return new CompilationUnit(tokenizer.getSource(), stats);
    }

    private Statement parseStatement() {
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
        if (match(SEMI)) {
            return new Block(position, Collections.emptyList()); // todo: Убрать рекурсию
        }
        if (match(SWITCH)) {
            return parseSwitch(position);
        }
        if (match(WHILE)) {
            return new WhileLoop(position, parseExpression(), parseStatement());
        }
        return parseUnusedExpression();
    }

    private Statement parseBreak(int position) {
        expect(SEMI);
        return new Break(position);
    }

    private Statement parseConst(int position) {
        List<ConstDef.Definition> definitions = new LinkedList<>();

        do {
            Token name = currentToken;
            expect(IDENTIFIER, EQ);
            definitions.add(new ConstDef.Definition(new Name(name.value(), name.pos), parseExpression()));
        } while (match(COMMA));

        expect(SEMI);
        return new ConstDef(position, definitions);
    }

    private Statement parseContinue(int position) {
        expect(SEMI);
        return new Continue(position);
    }

    private Statement parseDo(int position) {
        Statement body = parseStatement();
        expect(WHILE);
        Expression cond = parseExpression();
        expect(SEMI);
        return new DoLoop(position, body, cond);
    }

    private Statement parseFallthrough(int position) {
        expect(SEMI);
        return new Fallthrough(position);
    }

    private Statement parseFunction(int pos) {
        Name funcName = new Name(currentToken.value(), currentToken.pos);
        expect(IDENTIFIER, LPAREN);
        List<FuncDef.Parameter> params = new LinkedList<>();
        boolean comma = false, optionalState = false;

        while (!match(RPAREN)) {
            if (match(EOF) || comma && !match(COMMA)) {
                expect(RPAREN);
            }
            Token name0 = currentToken;
            expect(IDENTIFIER);
            Name name1 = new Name(name0.value(), name0.pos);
            Expression optional = null;

            if (match(EQ)) {
                optional = parseExpression();
                optionalState = true;
            } else if (optionalState) {
                pError(name0.pos, "here must be a optional argument.");
            }
            params.add(new FuncDef.Parameter(name1, optional));
            comma = !match(COMMA);
        }
        Statement body = parseBody();
        return new FuncDef(pos, funcName, params, body);
    }

    private Statement parseBody() {
        int pos = currentToken.pos;
        if (match(LBRACE)) return parseBlock(pos);
        if (match(EQ)) {
            Expression expr = parseExpression();
            expect(SEMI);
            return expr;
        }
        pError(pos, "Illegal function body");
        return null;
    }

    private Statement parseFor(int position) {
        boolean parens = match(LPAREN);
        List<Expression> init = null;

        if (!match(SEMI)) {
            init = parseExpressions();
            expect(SEMI);
        }
        Expression cond = null;

        if (!match(SEMI)) {
            cond = parseExpression();
            expect(SEMI);
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
        return new ForLoop(position, init, cond, step, parseStatement());
    }

    private Statement parseIf(int position) {
        Expression cond = parseExpression();
        Statement body = parseStatement();

        if (!match(ELSE)) {
            return new If(position, cond, body, null);
        }
        return new If(position, cond, body, parseStatement());
    }

    private Statement parseBlock(int position) {
        List<Statement> statements = new LinkedList<>();

        while (!match(RBRACE)) {
            if (match(EOF)) {
                expect(RBRACE);
            }
            statements.add(parseStatement());
        }
        return new Block(position, statements);
    }

//    private Statement parsePrint(int position) {
//        List<Expression> expressions = parseExpressions();
//        expect(SEMI);
//        return new PrintStatement(position, expressions);
//    }
//
//    private Statement parsePrintln(int position) {
//        List<Expression> expressions = parseExpressions();
//        expect(SEMI);
//        return new PrintlnStatement(position, expressions);
//    }

    private Statement parseReturn(int position) {
        if (match(SEMI)) {
            return new Return(position, null);
        }
        Expression expr = parseExpression();
        expect(SEMI);
        return new Return(position, expr);
    }

    private Statement parseSwitch(int position) {
        Expression selector = parseExpression();
        List<Case> cases = new LinkedList<>();
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
        return new Switch(position, selector, cases);
    }

    private Case parseCase(int position, boolean isDefault) {
        List<Expression> expressions = null;

        if (!isDefault) {
            expressions = parseExpressions();
        }
        expect(COL);
        return new Case(position, expressions, parseStatement());
    }

    private Statement parseUnusedExpression() {
        int position = currentToken.pos;
        Expression expr = parseExpression();
        expect(SEMI);
        return new Discarded(position, expr);
    }

    private List<Expression> parseExpressions() {
        List<Expression> expressions = new LinkedList<>();

        do {
            expressions.add(parseExpression());
        } while (match(COMMA));

        return expressions;
    }

    private Expression parseExpression() {
        return parseAssignment();
    }

    private Expression parseAssignment() {
        Expression expr = parseNullCoalesce();
        int position = currentToken.pos;

        if (match(AMPEQ)) {
            return new CompoundAssign(position, Tag.ASG_AND, expr, parseAssignment());
        }
        if (match(BAREQ)) {
            return new CompoundAssign(position, Tag.ASG_OR, expr, parseAssignment());
        }
        if (match(CARETEQ)) {
            return new CompoundAssign(position, Tag.XOR, expr, parseAssignment());
        }
        if (match(EQ)) {
            return new Assign(position, expr, parseAssignment());
        }
        if (match(GTGTEQ)) {
            return new CompoundAssign(position, Tag.ASG_SL, expr, parseAssignment());
        }
        if (match(LTLTEQ)) {
            return new CompoundAssign(position, Tag.ASG_SR, expr, parseAssignment());
        }
        if (match(MINUSEQ)) {
            return new CompoundAssign(position, Tag.ASG_SUB, expr, parseAssignment());
        }
        if (match(PERCENTEQ)) {
            return new CompoundAssign(position, Tag.ASG_REM, expr, parseAssignment());
        }
        if (match(PLUSEQ)) {
            return new CompoundAssign(position, Tag.ASG_ADD, expr, parseAssignment());
        }
        if (match(QUESQUESEQ)) {
            return new CompoundAssign(position, Tag.ASG_NULLCOALESCE, expr, parseAssignment());
        }
        if (match(SLASHEQ)) {
            return new CompoundAssign(position, Tag.ASG_DIV, expr, parseAssignment());
        }
        if (match(STAREQ)) {
            return new CompoundAssign(position, Tag.ASG_MUL, expr, parseAssignment());
        }
        return expr;
    }

    private Expression parseNullCoalesce() {
        Expression expr = parseTernary();

        while (true) {
            int position = currentToken.pos;

            if (match(QUESQUES)) {
                expr = new BinaryOp(position, Tag.NULLCOALESCE, expr, parseTernary());
            } else {
                return expr;
            }
        }
    }

    private Expression parseTernary() {
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

    private Expression parseTernary0(int position, Expression cond) {
        Expression right = parseExpression();
        expect(COL);
        return new TernaryOp(position, cond, right, parseExpression());
    }

    private Expression parseOr() {
        Expression expr = parseAnd();
        int position = currentToken.pos;

        while (match(BARBAR)) {
            expr = new BinaryOp(position, Tag.FLOW_OR, expr, parseAnd());
            position = currentToken.pos;
        }
        return expr;
    }

    private Expression parseAnd() {
        Expression expr = parseBitOr();

        while (true) {
            int position = currentToken.pos;

            if (match(AMPAMP)) {
                expr = new BinaryOp(position, Tag.FLOW_AND, expr, parseEquality());
            } else {
                return expr;
            }
        }
    }


    private Expression parseBitOr() {
        Expression expr = parseBitXor();

        while (true) {
            int position = currentToken.pos;

            if (match(BAR)) {
                expr = new BinaryOp(position, Tag.OR, expr, parseBitXor());
            } else {
                return expr;
            }
        }
    }

    private Expression parseBitXor() {
        Expression expr = parseBitAnd();

        while (true) {
            int position = currentToken.pos;

            if (match(CARET)) {
                expr = new BinaryOp(position, Tag.XOR, expr, parseBitAnd());
            } else {
                return expr;
            }
        }
    }

    private Expression parseBitAnd() {
        Expression expr = parseEquality();

        while (true) {
            int position = currentToken.pos;

            if (match(AMP)) {
                expr = new BinaryOp(position, Tag.AND, expr, parseEquality());
            } else {
                return expr;
            }
        }
    }

    private Expression parseEquality() {
        Expression expr = parseConditional();

        while (true) {
            int position = currentToken.pos;

            if (match(EQEQ)) {
                expr = new BinaryOp(position, Tag.EQ, expr, parseConditional());
            } else if (match(BANGEQ)) {
                expr = new BinaryOp(position, Tag.NE, expr, parseConditional());
            } else {
                return expr;
            }
        }
    }

    private Expression parseConditional() {
        Expression expr = parseShift();

        while (true) {
            int position = currentToken.pos;

            if (match(GT)) {
                expr = new BinaryOp(position, Tag.GT, expr, parseShift());
            } else if (match(GTEQ)) {
                expr = new BinaryOp(position, Tag.GE, expr, parseShift());
            } else if (match(LT)) {
                expr = new BinaryOp(position, Tag.LT, expr, parseShift());
            } else if (match(LTEQ)) {
                expr = new BinaryOp(position, Tag.LE, expr, parseShift());
            } else {
                return expr;
            }
        }
    }

    private Expression parseShift() {
        Expression expr = parseAdditive();

        while (true) {
            int position = currentToken.pos;

            if (match(GTGT)) {
                expr = new BinaryOp(position, Tag.SR, expr, parseAdditive());
            } else if (match(LTLT)) {
                expr = new BinaryOp(position, Tag.SL, expr, parseAdditive());
            } else {
                return expr;
            }
        }
    }

    private Expression parseAdditive() {
        Expression expr = parseMultiplicative();

        while (true) {
            int position = currentToken.pos;

            if (match(MINUS)) {
                expr = new BinaryOp(position, Tag.SUB, expr, parseMultiplicative());
            } else if (match(PLUS)) {
                expr = new BinaryOp(position, Tag.ADD, expr, parseMultiplicative());
            } else {
                return expr;
            }
        }
    }

    private Expression parseMultiplicative() {
        Expression expr = parseUnary();

        while (true) {
            int position = currentToken.pos;

            if (match(PERCENT)) {
                expr = new BinaryOp(position, Tag.REM, expr, parseUnary());
            } else if (match(SLASH)) {
                expr = new BinaryOp(position, Tag.DIV, expr, parseUnary());
            } else if (match(STAR)) {
                expr = new BinaryOp(position, Tag.MUL, expr, parseUnary());
            } else {
                return expr;
            }
        }
    }

    private Expression parseUnary() {
        int position = currentToken.pos;

        if (match(BANG)) {
            return new UnaryOp(position, Tag.NOT, parseUnary());
        }
        if (match(MINUS)) {
            return new UnaryOp(position, Tag.NEG, parseUnary());
        }
        if (match(MINUSMINUS)) {
            return new UnaryOp(position, Tag.PREDEC, parseUnary());
        }
        if (match(PLUS)) {
            return new UnaryOp(position, Tag.POS, parseUnary());
        }
        if (match(PLUSPLUS)) {
            return new UnaryOp(position, Tag.PREINC, parseUnary());
        }
        if (match(TILDE)) {
            return new UnaryOp(position, Tag.INVERSE, parseUnary());
        }
        return parseClone();
    }

    private Expression parseClone() {
        int position = currentToken.pos;

        // HACK todo
        if (currentToken.type == IDENTIFIER && currentToken.value().equals("clone")) {
            next();
            return new UnaryOp(position, Tag.CLONE, parsePost());
        }
        return parsePost();
    }

    private Expression parsePost() {
        Expression expr = parseAccess();

        while (true) {
            int position = currentToken.pos;

            if (match(MINUSMINUS)) {
                expr = new UnaryOp(position, Tag.POSTDEC, expr);
            } else if (match(PLUSPLUS)) {
                expr = new UnaryOp(position, Tag.POSTINC, expr);
            } else {
                return expr;
            }
        }
    }

    private Expression parseAccess() {
//        Expression expr = parsePrimary();
//        int position = currentToken.position;
//        int i = match(DOT) ? 1 : match(LBRACKET) ? 0 : -1;
//        return (i >= 0 ? parseArrayAccess(position, expr, i) : expr);

        Expression expression = parsePrimary();

        while (true) {
            int position = currentToken.pos;

            if (match(DOT)) {
                Token token = currentToken;
                expect(IDENTIFIER);
                expression = new FieldAccess(position, expression,
                        new Name(token.value(), token.pos));
            } else if (match(LBRACKET)) {
                expression = new ArrayAccess(position, expression, parseExpression());
                expect(RBRACKET);
            } else {
                break;
            }
        }
        return expression;
    }

//    private Expression parseArrayAccess(int position, Expression expr, int dot)
//            {
//        List<Expression> keys = new LinkedList<>();
//
//        while (true) {
//            if ((dot == 1) || match(DOT)) {
//                Token key = currentToken;
//                expect(IDENTIFIER);
//                keys.add(new StringExpression(currentToken.position, key.value()));
//                dot = 2;
//            } else if ((dot == 0) || match(LBRACKET)) {
//                keys.add(parseExpression());
//                expect(RBRACKET);
//                dot = 2;
//            } else {
//                return new ArrayAccess(position, expr, keys);
//            }
//        }
//    }

    private Expression parsePrimary() {
        Token token = currentToken;

        if (match(EOF)) {
            pError(token.pos, "missing expected expression.");
        }
        if (match(FALSE)) {
            return new Literal(token.pos, types.asBoolean(false));
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
            return new Literal(token.pos, types.asNull());
        }
        if (match(STRINGLITERAL)) {
            return new Literal(token.pos, types.asString(token.value()));
        }
        if (match(TRUE)) {
            return new Literal(token.pos, types.asBoolean(true));
        }
        unexpected(currentToken);
        return null;
    }

    private Expression parseFloat(Token token) {
        double d = Double.parseDouble(token.value());
        if (Double.isInfinite(d)) {
            pError(token.pos, "number too large.");
        }
        // todo: Может проверку стоит убрать?
        if ((d == 0.0) && !token.value().matches("\\.?0\\.?\\d*(?:[Ee][+-]\\d+)?$")) {
            pError(token.pos, "number too small.");
        }
        return new Literal(token.pos, types.asDouble(d));
    }

    private Expression parseIdentifier(Token token) {
        if (match(LPAREN)) {
            return parseInvocation(token);
        }
        return new Var(token.pos, new Name(token.value(), token.pos));
    }

    private Expression parseInvocation(Token token) {
        List<Invocation.Argument> args = new LinkedList<>();
        boolean comma = false;

        while (!match(RPAREN)) {
            if (match(EOF) || comma && !match(COMMA)) {
                expect(RPAREN);
            }
            // todo: именные аргументы
            // Название аргумента равно null по умолчанию.
            args.add(new Invocation.Argument(null, parseExpression()));
            comma = !match(COMMA);
        }
        return new Invocation(token.pos, new Name(token.value(), currentToken.pos), args);
    }

    private Expression parseInt(Token token) {
        try {
            return new Literal(token.pos, types.asLong(Long.parseLong(token.value())));
        } catch (NumberFormatException e) {
            pError(token.pos, "number too large.");
            return null;
        }
    }

    private Expression parseArray(int position, TokenType enclosing) {
        List<ArrayLiteral.Entry> entries = new LinkedList<>();
        boolean comma = false;

        while (!match(enclosing)) {
            if (match(EOF) || comma && !match(COMMA)) {
                expect(enclosing);
            }
            Expression key = null;
            Expression value = parseExpression();

            if (match(COL)) {
                key = value;
                value = parseExpression();
            }
            entries.add(new ArrayLiteral.Entry(key, value));
            comma = !match(COMMA);
        }
        return new ArrayLiteral(position, entries);
    }

    private Expression parseParens(int position) {
        Expression expr = parseExpression();
        expect(RPAREN);
        return new Parens(position, expr);
    }

    private void next() {
        currentToken = tokenizer.nextToken();
    }

    private boolean match(TokenType type) {
        if (currentToken.type == type) {
            next();
            return true;
        }
        return false;
    }

    private void expect(TokenType... types) {
        for (TokenType type : types) {
            if (match(type)) {
                continue;
            }
            //next();

            if (type.value == null) {
                if (type == IDENTIFIER) {
                    pError(currentToken.pos, "identifier expected.");
                } else {
                    unexpected(currentToken);
                }
                return;
            }
            if (currentToken.getClass() == Token.class) {
                pError(currentToken.pos, type + " expected.");
                return;
            }
            pError(currentToken.pos, type + " expected, " + currentToken + " found.");
        }
    }

    private void unexpected(Token token) {
        pError(token.pos, "unexpected " + token + '.');
    }

    private void pError(int position, String message) {
        log.error(position, message);
    }
}
