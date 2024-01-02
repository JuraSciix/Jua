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
import static jua.compiler.TreeInfo.getBinOpPrecedence;
import static jua.compiler.TreeInfo.getUnaryOpTag;

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
        Flow.Builder<Stmt> stats = Flow.builder();
        Flow.Builder<FuncDef> funcDefs = Flow.builder();
        nextToken();

        while (!acceptToken(EOF)) {
            try {
                if (acceptToken(FN)) {
                    funcDefs.append(parseFunction());
                    continue;
                }
                stats.append(parseStatement());
            } catch (ParseNodeExit e) {
                report(e);
            }
        }
        return new Document(0, source,
                funcDefs.toFlow(),
                stats.toFlow());
    }

    private void report(ParseNodeExit e) {
        log.error(source, e.pos, e.msg);
    }

    public Stmt parseStatement() {
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
            Expr init = null;
            if (acceptToken(EQ)) {
                init = parseExpression();
            }
            defs.append(new VarDef.Definition(name.pos, name.name(), init));
        } while (acceptToken(COMMA));
        expectToken(SEMI);
        return new VarDef(acceptedPos, defs.toFlow());
    }

    private Stmt parseBreak() {
        expectToken(SEMI);
        return new Break(acceptedPos);
    }

    private Stmt parseContinue() {
        expectToken(SEMI);
        return new Continue(acceptedPos);
    }

    private Stmt parseDo() {
        int position = acceptedPos;
        Stmt body = parseStatement();
        expectToken(WHILE);
        Expr cond = parseExpression();
        expectToken(SEMI);
        return new DoLoop(position, body, cond);
    }

    private Stmt parseFallthrough() {
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
            Expr optional = null;

            if (acceptToken(EQ)) {
                optional = parseExpression();
                optionalState = true;
            } else if (optionalState) {
                reportError(p.pos, "here must be a optional argument.");
            }
            params.append(new FuncDef.Parameter(p.pos, p.name(), optional));
            comma = !acceptToken(COMMA);
        }
        Stmt body = parseBody();
        return new FuncDef(pos, funcName.pos, funcName.name(), params.toFlow(), body);
    }

    private Stmt parseBody() {
        int pos = token.pos;
        if (acceptToken(LBRACE)) return parseBlock();
        if (acceptToken(EQ)) {
            Expr expr = parseExpression();
            expectToken(SEMI);
            return new Discarded(expr.pos, expr);
        }
        reportError(pos, "Illegal function body");
        return null;
    }

    private Flow<Stmt> parseForInit() {
        Flow.Builder<Stmt> init = Flow.builder();
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

    private Stmt parseFor() {
        int position = acceptedPos;
        boolean parens = acceptToken(LPAREN);
        Flow<Stmt> init = acceptToken(SEMI) ? null : parseForInit();

        Expr cond = null;

        if (!acceptToken(SEMI)) {
            cond = parseExpression();
            expectToken(SEMI);
        }
        Flow<Expr> step = null;

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

    private Stmt parseIf() {
        int position = acceptedPos;
        Expr cond = parseExpression();
        Stmt body = parseStatement();

        if (!acceptToken(ELSE)) {
            return new If(position, cond, body, null);
        }
        return new If(position, cond, body, parseStatement());
    }

    private Stmt parseBlock() {
        int pos = acceptedPos;
        Flow.Builder<Stmt> statements = Flow.builder();

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

    private Stmt parseReturn() {
        if (acceptToken(SEMI)) {
            return new Return(acceptedPos, null);
        }
        int pos = acceptedPos;
        Expr expr = null;
        try {
            expr = parseExpression();
        } catch (ParseNodeExit e) {
            report(e);
        }
        expectToken(SEMI);
        return new Return(pos, expr);
    }

    private Stmt parseSwitch() {
        int position = acceptedPos;
        Expr selector = parseExpression();
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
        Flow<Expr> expressions = null;

        if (!isDefault) {
            expressions = parseExpressions();
        }
        expectToken(ARROW);
        Stmt body = parseStatement();
        return new Case(position, expressions, body);
    }

    private Stmt parseUnusedExpression() {
        int position = acceptedPos;
        Expr expr = parseExpression();
        expectToken(SEMI);
        return new Discarded(position, expr);
    }

    public Expr parseExpression() {
        return parseAssignment();
    }

    private Flow<Expr> parseExpressions() {
        Flow.Builder<Expr> expressions = Flow.builder();

        do {
            expressions.append(parseExpression());
        } while (acceptToken(COMMA));

        return expressions.toFlow();
    }

    Expr parseAssignment() {
        Expr expr = parseConditional();
        int pos = token.pos;

        if (acceptToken(EQ))
            return new Assign(pos, expr, parseAssignment());

        if (matchesEnhancedAsgOp()) {
            Tag tag = TreeInfo.getAsgTag(token.type);
            nextToken();
            return new EnhancedAssign(pos, tag, expr, parseAssignment());
        }

        return expr;
    }

    private boolean matchesEnhancedAsgOp() {
        TokenType type = token.type;
        return type.compareTo(AMPEQ) >= 0 && 0 >= type.compareTo(STAREQ);
    }

    Expr parseConditional() {
        Expr expr = parseBinary();

        while (true) {
            int position = token.pos;

            if (acceptToken(QUES)) {
                expr = parseConditional0(position, expr);
            } else {
                return expr;
            }
        }
    }

    private Expr parseConditional0(int position, Expr cond) {
        Expr right = parseExpression();
        expectToken(COL);
        return new Conditional(position, cond, right, parseExpression());
    }

    Expr parseBinary() {
        Expr lhs = parseUnary();
        BinaryOp prev = null;

        while (matchesBinOp()) {
            int pos = acceptedPos;
            Tag tag = TreeInfo.getBinOpTag(token.type);
            nextToken();
            Expr rhs = parseUnary();

            // [x + y] * z ===> x + [y * z]
            if (prev != null && getBinOpPrecedence(prev.tag) < getBinOpPrecedence(tag)) {
                // Модифицируем дерево без лишних аллокаций.
                prev.rhs = new BinaryOp(pos, tag, prev.rhs, rhs);
            } else {
                lhs = prev = new BinaryOp(pos, tag, lhs, rhs);
            }
        }

        return lhs;
    }

    private boolean matchesBinOp() {
        TokenType type = token.type;
        return type.compareTo(AMP) >= 0 && 0 >= type.compareTo(PLUS);
    }

    Expr parseUnary() {
        int pos = token.pos;

        if (matchesUnaryOp()) {
            Tag tag = getUnaryOpTag(token.type);
            nextToken();
            return new UnaryOp(pos, tag, parseUnary());
        }

        return parsePost();
    }

    private boolean matchesUnaryOp() {
        TokenType type = token.type;
        return type.compareTo(BANG) >= 0 && 0 >= type.compareTo(AT) ||
                type == PLUS || type == MINUS;
    }

    Expr parsePost() {
        Expr expr = parseCall();

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

    Expr parseCall() {
        int pos = token.pos;
        Expr expr = parseAccess();

        if (acceptToken(LPAREN)) {
            Flow<Invocation.Argument> args = parseInvocationArgs();
            expectToken(RPAREN);
            return new Invocation(pos, expr, args);
        }

        return expr;
    }

    Expr parseAccess() {
        Expr expr = parsePrimary();

        while (acceptToken(LBRACKET)) {
            int pos = acceptedPos;
            Expr index = parseExpression();
            expectToken(RBRACKET);
            expr = new Access(pos, expr, index);
        }

        return expr;
    }

    Expr parsePrimary() {
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

    private Expr parseFloat(Token token) {
        double d = Conversions.parseDouble(token.value(), token.radix());
        if (Double.isInfinite(d)) {
            reportError(token.pos, "number too large.");
        }
        return new Literal(token.pos, d);
    }

    private Expr parseIdentifier(Token token) {
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
            Expr expr;
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

    private Expr parseInvocation(Token token) {
        Flow<Invocation.Argument> args = parseInvocationArgs();
        expectToken(RPAREN);
        return new Invocation(token.pos,
                new MemberAccess(token.pos, Tag.MEMACCESS, null, token.pos, token.name()),
                args);
    }

    private Expr parseInt(Token token) {
        try {
            long value = Conversions.parseLong(token.value(), token.radix());
            return new Literal(token.pos, value);
        } catch (NumberFormatException e) {
            reportError(token.pos, "number too large.");
            return null;
        }
    }

    private ListLiteral parseListInit(int pos) {
        Flow.Builder<Expr> entries = Flow.builder();
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

    private Expr parseParens() {
        int pos = acceptedPos;
        Expr expr = parseExpression();
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
