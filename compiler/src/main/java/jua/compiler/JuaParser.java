package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;
import jua.compiler.Tree.*;
import jua.compiler.utils.Flow;
import jua.compiler.utils.Conversions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static jua.compiler.Tokens.TokenType.*;

public final class JuaParser {

    // todo: Рефакторинг и оптимизация.

    private static class ParseNodeExit extends Error {

        final int pos;

        final String msg;

        ParseNodeExit(int pos, String msg) {
            this.pos = pos;
            this.msg = msg;
        }
    }

    private final Source source;

    private final Log log;

    private final Lexer tokenizer;

    Token token;

    int acceptedPos;

    public JuaParser(Source source, Log log) {
        this.source = source;
        this.log = log;
        tokenizer = new Lexer(source, log);
    }

    public Document parseDocument() {
        Flow.Builder<Statement> stats = Flow.builder();
        Flow.Builder<FuncDef> funcDefs = Flow.builder();
        Flow.Builder<ConstDef> constDefs = Flow.builder();
        nextToken();

        while (!acceptToken(EOF)) {
            try {
                if (acceptToken(FN)) {
                    funcDefs.append(parseFunction());
                    continue;
                }
                if (acceptToken(CONST)) {
                    constDefs.append(parseConst());
                    continue;
                }
                stats.append(parseStatement());
            } catch (ParseNodeExit e) {
                report(e);
            }
        }
        return new Document(0, source,
                constDefs.toFlow(),
                funcDefs.toFlow(),
                stats.toFlow());
    }

    private void report(ParseNodeExit e) {
        log.error(source, e.pos, e.msg);
    }

    public Statement parseStatement() {
        acceptedPos = token.pos;

        switch (token.type) {
            case BREAK: {
                nextToken();
                return parseBreak();
            }
            case CONST: {
                nextToken();
                reportError(acceptedPos, "constant declaration is not allowed here");
            }
            case CONTINUE: {
                nextToken();
                return parseContinue();
            }
            case DO: {
                nextToken();
                return parseDo();
            }
            case ELSE: {
                nextToken();
                reportError(acceptedPos, "'else' is not allowed without if-statement.");
            }
            case EOF: {
                nextToken();
                reportError(acceptedPos, "missing expected statement.");
            }
            case FALLTHROUGH: {
                nextToken();
                return parseFallthrough();
            }
            case FN: {
                nextToken();
                reportError(acceptedPos, "function declaration is not allowed here");
            }
            case FOR: {
                nextToken();
                return parseFor();
            }
            case IF: {
                nextToken();
                return parseIf();
            }
            case LBRACE: {
                nextToken();
                return parseBlock();
            }
            case RETURN: {
                nextToken();
                return parseReturn();
            }
            case SEMI: {
                nextToken();
                return new Block(acceptedPos, null);
            }
            case SWITCH: {
                nextToken();
                return parseSwitch();
            }
            case VAR: {
                nextToken();
                return parseVar();
            }
            case WHILE: {
                nextToken();
                return new WhileLoop(acceptedPos, parseExpression(), parseStatement());
            }
            default: return parseUnusedExpression();
        }
    }

    private VarDef parseVar() {
        Flow.Builder<VarDef.Definition> defs = Flow.builder();
        do {
            Token name = token;
            expectToken(IDENTIFIER);
            Expression init = null;
            if (acceptToken(EQ)) {
                init = parseExpression();
            }
            defs.append(new VarDef.Definition(name.pos, name.name(), init));
        } while (acceptToken(COMMA));
        expectToken(SEMI);
        return new VarDef(acceptedPos, defs.toFlow());
    }

    private ConstDef parseConst() {
        Flow.Builder<ConstDef.Definition> definitions = Flow.builder();

        do {
            try {
                Token name = token;
                expectToken(IDENTIFIER);
                expectToken(EQ);
                definitions.append(new ConstDef.Definition(name.pos, name.name(), parseExpression()));
            } catch (ParseNodeExit e) {
                report(e);
            }
        } while (acceptToken(COMMA));

        expectToken(SEMI);
        return new ConstDef(acceptedPos, definitions.toFlow());
    }

    private Statement parseBreak() {
        expectToken(SEMI);
        return new Break(acceptedPos);
    }

    private Statement parseContinue() {
        expectToken(SEMI);
        return new Continue(acceptedPos);
    }

    private Statement parseDo() {
        int position = acceptedPos;
        Statement body = parseStatement();
        expectToken(WHILE);
        Expression cond = parseExpression();
        expectToken(SEMI);
        return new DoLoop(position, body, cond);
    }

    private Statement parseFallthrough() {
        expectToken(SEMI);
        return new Fallthrough(acceptedPos);
    }

    private FuncDef parseFunction() {
        int pos = acceptedPos;
        Token funcName = token;
        expectToken(IDENTIFIER);
        expectToken(LPAREN);
        Flow.Builder<FuncDef.Parameter> params = Flow.builder();
        boolean comma = false, optionalState = false;

        while (!acceptToken(RPAREN)) {
            if (acceptToken(EOF) || comma && !acceptToken(COMMA)) {
                expectToken(RPAREN);
            }
            Token p = token;
            expectToken(IDENTIFIER);
            Expression optional = null;

            if (acceptToken(EQ)) {
                optional = parseExpression();
                optionalState = true;
            } else if (optionalState) {
                reportError(p.pos, "here must be a optional argument.");
            }
            params.append(new FuncDef.Parameter(p.pos, p.name(), optional));
            comma = !acceptToken(COMMA);
        }
        Statement body = parseBody();
        return new FuncDef(pos, funcName.pos, funcName.name(), params.toFlow(), body);
    }

    private Statement parseBody() {
        int pos = token.pos;
        if (acceptToken(LBRACE)) return parseBlock();
        if (acceptToken(EQ)) {
            Expression expr = parseExpression();
            expectToken(SEMI);
            return new Discarded(expr.pos, expr);
        }
        reportError(pos, "Illegal function body");
        return null;
    }

    private Flow<Statement> parseForInit() {
        Flow.Builder<Statement> init = Flow.builder();
        if (acceptToken(VAR)) {
            init.append(parseVar());
        } else {
            try {
                Flow.forEach(parseExpressions(), expr -> init.append(new Discarded(expr.pos, expr)));
            } catch (ParseNodeExit e) {
                report(new ParseNodeExit(e.pos, "invalid statement"));
            }
            expectToken(SEMI);
        }

        return init.toFlow();
    }

    private Statement parseFor() {
        int position = acceptedPos;
        boolean parens = acceptToken(LPAREN);
        Flow<Statement> init = acceptToken(SEMI) ? null : parseForInit();

        Expression cond = null;

        if (!acceptToken(SEMI)) {
            cond = parseExpression();
            expectToken(SEMI);
        }
        Flow<Expression> step = null;

        if (parens) {
            if (!acceptToken(RPAREN)) {
                step = parseExpressions();
                expectToken(RPAREN);
            }
        } else {
            step = parseExpressions();
        }
        return new ForLoop(position, init, cond, step, parseStatement());
    }

    private Statement parseIf() {
        int position = acceptedPos;
        Expression cond = parseExpression();
        Statement body = parseStatement();

        if (!acceptToken(ELSE)) {
            return new If(position, cond, body, null);
        }
        return new If(position, cond, body, parseStatement());
    }

    private Statement parseBlock() {
        int pos = acceptedPos;
        Flow.Builder<Statement> statements = Flow.builder();

        while (!acceptToken(RBRACE)) {
            if (acceptToken(EOF)) {
                expectToken(RBRACE);
            }
            try {
                statements.append(parseStatement());
            } catch (ParseNodeExit e) {
                report(e);
            }
        }
        return new Block(pos, statements.toFlow());
    }

    private Statement parseReturn() {
        if (acceptToken(SEMI)) {
            return new Return(acceptedPos, null);
        }
        int pos = acceptedPos;
        Expression expr = null;
        try {
            expr = parseExpression();
        } catch (ParseNodeExit e) {
            report(e);
        }
        expectToken(SEMI);
        return new Return(pos, expr);
    }

    private Statement parseSwitch() {
        int position = acceptedPos;
        Expression selector = parseExpression();
        Flow.Builder<Case> cases = Flow.builder();
        expectToken(LBRACE);

        while (!acceptToken(RBRACE)) {
            try {
                int position1 = token.pos;
                Case c;
                if (acceptToken(ELSE)) {
                    c = parseCase(position1, true);
                } else {
                    c = parseCase(position1, false);
                }
                cases.append(c);
            } catch (ParseNodeExit e) {
                report(e);
            }
        }
        return new Switch(position, selector, cases.toFlow());
    }

    private Case parseCase(int position, boolean isDefault) {
        Flow<Expression> expressions = null;

        if (!isDefault) {
            expressions = parseExpressions();
        }
        expectToken(ARROW);
        Statement body = parseStatement();
        return new Case(position, expressions, body);
    }

    private Statement parseUnusedExpression() {
        int position = acceptedPos;
        Expression expr = parseExpression();
        expectToken(SEMI);
        return new Discarded(position, expr);
    }

    public Expression parseExpression() {
        return parseAssignment();
    }

    private Flow<Expression> parseExpressions() {
        Flow.Builder<Expression> expressions = Flow.builder();

        do {
            expressions.append(parseExpression());
        } while (acceptToken(COMMA));

        return expressions.toFlow();
    }

    private Expression parseAssignment() {
        Expression expr = parseConditional();
        int position = token.pos;

        switch (token.type) {
            case EQ:
                nextToken();
                return new Assign(position, expr, parseAssignment());

            case AMPEQ:
            case BAREQ:
            case CARETEQ:
            case GTGTEQ:
            case LTLTEQ:
            case MINUSEQ:
            case PERCENTEQ:
            case PLUSEQ:
            case QUESQUESEQ:
            case SLASHEQ:
            case STAREQ:
                nextToken();
                return new EnhancedAssign(position, TreeInfo.getAsgTag(token.type), expr, parseAssignment());

            default:
                return expr;
        }
    }

    private Expression parseConditional() {
        Expression expr = parseOr();

        while (true) {
            int position = token.pos;

            if (acceptToken(QUES)) {
                expr = parseConditional0(position, expr);
            } else {
                return expr;
            }
        }
    }

    private Expression parseConditional0(int position, Expression cond) {
        Expression right = parseExpression();
        expectToken(COL);
        return new Conditional(position, cond, right, parseExpression());
    }

    private Expression parseOr() {
        Expression expr = parseAnd();
        int position = token.pos;

        while (acceptToken(BARBAR)) {
            expr = new BinaryOp(position, Tag.OR, expr, parseAnd());
            position = token.pos;
        }
        return expr;
    }

    private Expression parseAnd() {
        Expression expr = parseBitOr();

        while (true) {
            int position = token.pos;

            if (acceptToken(AMPAMP)) {
                expr = new BinaryOp(position, Tag.AND, expr, parseEquality());
            } else {
                return expr;
            }
        }
    }


    private Expression parseBitOr() {
        Expression expr = parseBitXor();

        while (true) {
            int position = token.pos;

            if (acceptToken(BAR)) {
                expr = new BinaryOp(position, Tag.BIT_OR, expr, parseBitXor());
            } else {
                return expr;
            }
        }
    }

    private Expression parseBitXor() {
        Expression expr = parseBitAnd();

        while (true) {
            int position = token.pos;

            if (acceptToken(CARET)) {
                expr = new BinaryOp(position, Tag.BIT_XOR, expr, parseBitAnd());
            } else {
                return expr;
            }
        }
    }

    private Expression parseBitAnd() {
        Expression expr = parseEquality();

        while (true) {
            int position = token.pos;

            if (acceptToken(AMP)) {
                expr = new BinaryOp(position, Tag.BIT_AND, expr, parseEquality());
            } else {
                return expr;
            }
        }
    }

    private Expression parseEquality() {
        Expression expr = parseComparison();

        while (true) {
            int position = token.pos;

            if (acceptToken(EQEQ)) {
                expr = new BinaryOp(position, Tag.EQ, expr, parseComparison());
            } else if (acceptToken(BANGEQ)) {
                expr = new BinaryOp(position, Tag.NE, expr, parseComparison());
            } else {
                return expr;
            }
        }
    }

    private Expression parseComparison() {
        Expression expr = parseShift();

        while (true) {
            int position = token.pos;

            if (acceptToken(GT)) {
                expr = new BinaryOp(position, Tag.GT, expr, parseShift());
            } else if (acceptToken(GTEQ)) {
                expr = new BinaryOp(position, Tag.GE, expr, parseShift());
            } else if (acceptToken(LT)) {
                expr = new BinaryOp(position, Tag.LT, expr, parseShift());
            } else if (acceptToken(LTEQ)) {
                expr = new BinaryOp(position, Tag.LE, expr, parseShift());
            } else {
                return expr;
            }
        }
    }

    private Expression parseShift() {
        Expression expr = parseAdditive();

        while (true) {
            int position = token.pos;

            if (acceptToken(GTGT)) {
                expr = new BinaryOp(position, Tag.SR, expr, parseAdditive());
            } else if (acceptToken(LTLT)) {
                expr = new BinaryOp(position, Tag.SL, expr, parseAdditive());
            } else {
                return expr;
            }
        }
    }

    private Expression parseAdditive() {
        Expression expr = parseMultiplicative();

        while (true) {
            int position = token.pos;

            if (acceptToken(MINUS)) {
                expr = new BinaryOp(position, Tag.SUB, expr, parseMultiplicative());
            } else if (acceptToken(PLUS)) {
                expr = new BinaryOp(position, Tag.ADD, expr, parseMultiplicative());
            } else {
                return expr;
            }
        }
    }

    private Expression parseMultiplicative() {
        Expression expr = parseCoalesce();

        while (true) {
            int position = token.pos;

            if (acceptToken(PERCENT)) {
                expr = new BinaryOp(position, Tag.REM, expr, parseCoalesce());
            } else if (acceptToken(SLASH)) {
                expr = new BinaryOp(position, Tag.DIV, expr, parseCoalesce());
            } else if (acceptToken(STAR)) {
                expr = new BinaryOp(position, Tag.MUL, expr, parseCoalesce());
            } else {
                return expr;
            }
        }
    }

    private Expression parseCoalesce() {
        Expression expr = parseUnary();
        int pos = token.pos;

        if (acceptToken(QUESQUES)) {
            expr = new BinaryOp(pos, Tag.COALESCE, expr, parseCoalesce());
        }
        return expr;
    }

    private Expression parseUnary() {
        int position = token.pos;

        if (acceptToken(BANG)) {
            return new UnaryOp(position, Tag.NOT, parseUnary());
        }
        if (acceptToken(MINUS)) {
            return new UnaryOp(position, Tag.NEG, parseUnary());
        }
        if (acceptToken(MINUSMINUS)) {
            return new UnaryOp(position, Tag.PREDEC, parseUnary());
        }
        if (acceptToken(PLUS)) {
            return new UnaryOp(position, Tag.POS, parseUnary());
        }
        if (acceptToken(PLUSPLUS)) {
            return new UnaryOp(position, Tag.PREINC, parseUnary());
        }
        if (acceptToken(TILDE)) {
            return new UnaryOp(position, Tag.BIT_INV, parseUnary());
        }
        return parsePost();
    }

    private Expression parsePost() {
        Expression expr = parseCall();

        while (true) {
            int position = token.pos;

            if (acceptToken(MINUSMINUS)) {
                expr = new UnaryOp(position, Tag.POSTDEC, expr);
            } else if (acceptToken(PLUSPLUS)) {
                expr = new UnaryOp(position, Tag.POSTINC, expr);
            } else {
                return expr;
            }
        }
    }

    private Expression parseCall() {
        int pos = token.pos;
        Expression expr = parseAccess();

        if (acceptToken(LPAREN)) {
            Flow<Invocation.Argument> args = parseInvocationArgs();
            expectToken(RPAREN);
            return new Invocation(pos, expr, args);
        }

        return expr;
    }

    private Expression parseAccess() {
        Expression expr = parsePrimary();

        while (true) {
            Token op = token;
            switch (op.type) {
                case DOT:
                case QUESDOT:
                    nextToken();
                    Token member = token;
                    expectToken(IDENTIFIER);
                    expr = new MemberAccess(op.pos,
                            (op.type == DOT) ? Tag.MEMACCESS : Tag.MEMACCSF,
                            expr, member.pos, member.name());
                    break;

                case LBRACKET:
                case QUESLBRACKET:
                    nextToken();
                    Expression index = parseExpression();
                    expectToken(RBRACKET);
                    expr = new ArrayAccess(op.pos,
                            (op.type == LBRACKET) ? Tag.ARRACC : Tag.ARRACCSF,
                            expr, index);
                    break;

                default: return expr;
            }
        }
    }

    private Expression parsePrimary() {
        acceptedPos = token.pos;
        Token tok = token;
        nextToken();

        switch (tok.type) {
            case EOF: {
                reportError(tok.pos, "missing expected expression.");
            }
            case FALSE: {
                return new Literal(tok.pos, false);
            }
            case FLOATLITERAL: {
                return parseFloat(tok);
            }
            case IDENTIFIER: {
                return parseIdentifier(tok);
            }
            case INTLITERAL: {
                return parseInt(tok);
            }
            case LBRACE: {
                return parseMapInit(tok.pos);
            }
            case LBRACKET: {
                return parseListInit(tok.pos);
            }
            case LPAREN: {
                return parseParens();
            }
            case NULL: {
                return new Literal(tok.pos, null);
            }
            case STRINGLITERAL: {
                return new Literal(tok.pos, tok.value());
            }
            case TRUE: {
                return new Literal(tok.pos, true);
            }
            default:
                unexpected(tok, Arrays.asList(
                        FALSE, FLOATLITERAL, IDENTIFIER,
                        INTLITERAL, LBRACE, LBRACKET,
                        LPAREN, NULL, STRINGLITERAL, TRUE
                ));
                return null; // UNREACHABLE
        }
    }

    private Expression parseFloat(Token token) {
        double d = Conversions.parseDouble(token.value(), token.radix());
        if (Double.isInfinite(d)) {
            reportError(token.pos, "number too large.");
        }
        return new Literal(token.pos, d);
    }

    private Expression parseIdentifier(Token token) {
        if (acceptToken(LPAREN)) {
            return parseInvocation(token);
        }
        return new Var(token.pos, token.name());
    }

    private Flow<Invocation.Argument> parseInvocationArgs() {
        Flow.Builder<Invocation.Argument> args = Flow.builder();
        boolean comma = false;

        while (!matchesToken(RPAREN)) {
            if (acceptToken(EOF) || comma && !acceptToken(COMMA)) {
                expectToken(RPAREN);
            }
            Token name = new Tokens.NamedToken(null, 0, null);
            Expression expr;
            if (matchesToken(IDENTIFIER)) {
                Token t = token;
                expr = parseExpression();
                if (expr.hasTag(Tag.VAR) && acceptToken(COL)) {
                    name = t;
                    expr = parseExpression();
                }
            } else {
                expr = parseExpression();
            }
            args.append(new Invocation.Argument(name.pos, name.name(), expr));
            comma = !acceptToken(COMMA);
        }

        return args.toFlow();
    }

    private Expression parseInvocation(Token token) {
        Flow<Invocation.Argument> args = parseInvocationArgs();
        expectToken(RPAREN);
        return new Invocation(token.pos,
                new MemberAccess(token.pos, Tag.MEMACCESS, null, token.pos, token.name()),
                args);
    }

    private Expression parseInt(Token token) {
        try {
            long value = Conversions.parseLong(token.value(), token.radix());
            return new Literal(token.pos, value);
        } catch (NumberFormatException e) {
            reportError(token.pos, "number too large.");
            return null;
        }
    }

    private ListLiteral parseListInit(int pos) {
        Flow.Builder<Expression> entries = Flow.builder();
        if (!acceptToken(RBRACKET)) {
            do {
                entries.append(parseExpression());
                if (acceptToken(COMMA)) {
                    if (acceptToken(RBRACKET)) {
                        break;
                    }
                    continue;
                }
                if (acceptToken(RBRACKET)) {
                    break;
                }
                unexpected(token, Arrays.asList(COMMA, RBRACKET));
            } while (true);
        }
        return new ListLiteral(pos, entries.toFlow());
    }

    private MapLiteral parseMapInit(int pos) {
        Flow.Builder<MapLiteral.Entry> entries
                = Flow.builder();
        if (!acceptToken(RBRACE)) {
            do {
                int entryPos = token.pos;
                Expression key;
                boolean field;
                if (acceptToken(LBRACKET)) {
                    key = parseExpression();
                    expectToken(RBRACKET);
                    field = false;
                } else {
                    switch (token.type) {
                        case FLOATLITERAL:
                            key = parseFloat(token);
                            break;
                        case INTLITERAL:
                            key = parseInt(token);
                            break;
                        case IDENTIFIER:
                            key = new Literal(token.pos, token.name());
                            break;
                        default:
                            unexpected(token, Arrays.asList(FLOATLITERAL, INTLITERAL, IDENTIFIER));
                            throw new AssertionError(); // UNREACHABLE
                    }
                    nextToken();
                    field = true;
                }
                expectToken(COL);
                Expression value = parseExpression();
                MapLiteral.Entry entry = new MapLiteral.Entry(entryPos, key, value);
                entry.field = field;
                entries.append(entry);
                if (acceptToken(COMMA)) {
                    if (acceptToken(RBRACE)) {
                        break;
                    }
                    continue;
                }
                if (acceptToken(RBRACE)) {
                    break;
                }
                unexpected(token, Arrays.asList(COMMA, RBRACE));
            } while (true);
        }
        return new MapLiteral(pos, entries.toFlow());
    }

    private Expression parseParens() {
        int pos = acceptedPos;
        Expression expr = parseExpression();
        expectToken(RPAREN);
        return new Parens(pos, expr);
    }

    private void nextToken() {
        token = tokenizer.nextToken();
    }

    private boolean matchesToken(TokenType type) {
        return token.type == type;
    }
    
    private boolean acceptToken(TokenType type) {
        if (matchesToken(type)) {
            acceptedPos = token.pos;
            nextToken();
            return true;
        }
        return false;
    }

    private void expectToken(TokenType type) {
        try {
            if (matchesToken(type)) return;
            // Compile error: X expected, Y found.
            // Compile error: X expected.
            // Compile error: unexpected Y.
            String expected = type2string(type);
            String found = token.type == EOF ? null : token2string(token);

            if (expected != null && found != null) {
                reportError(token.pos, expected + " expected, " + found + " found.");
                return;
            }

            if (expected != null) {
                reportError(token.pos, expected + " expected.");
                return;
            }

            if (found != null) {
                reportError(token.pos, "unexpected " + found + ".");
                return;
            }

            reportError(token.pos, "invalid syntax.");
        } finally {
            nextToken();
        }
    }

    static String token2string(Token token) {
        if (token.type == EOF) return null;
        return type2string(token.type);
    }

    static String type2string(TokenType type) {
        switch (type) {
            case IDENTIFIER:    return "name";
            case STRINGLITERAL: return "string";
            case INTLITERAL:    return "integer number";
            case FLOATLITERAL:  return "floating-point number";
            case INVALID:       return "invalid token";
            default: return type.value == null ? null : '\'' + type.value + '\'';
        }
    }

    private void unexpected(Token foundToken, List<TokenType> expectedTypes) {
        String expected = expectedTypes.stream()
                .map(JuaParser::type2string)
                .collect(Collectors.joining(", "));
        reportError(foundToken.pos, "unexpected " + token2string(foundToken) + ", expected instead: " + expected);
    }

    private void reportError(int position, String message) {
        // При возникновении ошибок, в AST попадают нулевые значения.
        // Это порождает множество потенциальных ошибок, в том числе NPE
        // для последующих стадий компиляции.
        // Решение прерывать парсинг узла с ошибкой
        // позволяет избежать нулевых значений в дереве, но
        // портит лексическую последовательность и, впоследствии,
        // семантическую последовательность ошибок. Качественных
        // решений последней проблемы пока что не вижу.
        throw new ParseNodeExit(position, message);
    }
}
