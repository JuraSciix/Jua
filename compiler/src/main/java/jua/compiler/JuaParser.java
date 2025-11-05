package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;
import jua.compiler.Tree.*;
import jua.compiler.utils.Conversions;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static jua.compiler.CompHelper.*;
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

    private final TokenStream tokenizer;

    Token token;

    int acceptedPos;

    public JuaParser(Source source, TokenStream ts) {
        this.source = source;
        this.log = source.getLog();
        tokenizer = ts;
    }

    public Document parseDocument() {
        TList<Stmt> stats = new TList<>();
        TList<FuncDef> funcDefs = new TList<>();
        nextToken();

        while (!acceptToken(EOF)) {
            try {
                if (acceptToken(FN)) {
                    funcDefs.add(parseFunction());
                    continue;
                }
                stats.add(parseStatement());
            } catch (ParseNodeExit e) {
                report(e);
            }
        }
        return new Document(0, source,
                funcDefs,
                stats);
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
                return new Block(acceptedPos, TList.empty());
            }
            case VAR: {
                nextToken();
                return parseVar();
            }
            case WHILE: {
                nextToken();
                return new WhileLoop(acceptedPos, parseExpression(), parseStatement());
            }
            case IDENTIFIER: {
                Token id = token;
                if (tokenizer.peek(0).type == COLEQ) {
                    nextToken();
                    nextToken();
                    Expr expr = parseExpression();
                    expectSemi();
                    return new VarDef(id.pos, TList.of(
                            new VarDef.Definition(id.pos, id.name(), expr)
                    ));
                }
                return parseUnusedExpression();
            }
            default: return parseUnusedExpression();
        }
    }

    private void expectSemi() {
        // todo: В будущем планируется сделать точку с запятой не обязательной,
        //  но есть проблема: становится сложнее определить конечный вид дерева.
        //  Например: if true { return } return while (true){}. Надо разделить токены на две группы:
        //   токены операторов и токены выражений.
        //  Или можно внеднить новый токен NEWLINE, но надо подумать.
        expectToken(SEMI);
    }

    private VarDef parseVar() {
        TList<VarDef.Definition> defs = new TList<>();
        do {
            Token name = token;
            expectToken(IDENTIFIER);
            Expr init = null;
            if (acceptToken(EQ)) {
                init = parseExpression();
            }
            defs.add(new VarDef.Definition(name.pos, name.name(), init));
        } while (acceptToken(COMMA));
        expectSemi();
        return new VarDef(acceptedPos, defs);
    }

    private Stmt parseBreak() {
        expectSemi();
        return new Break(acceptedPos);
    }

    private Stmt parseContinue() {
        expectSemi();
        return new Continue(acceptedPos);
    }

    private Stmt parseDo() {
        int position = acceptedPos;
        Stmt body = parseStatement();
        expectToken(WHILE);
        Expr cond = parseExpression();
        expectSemi();
        return new DoLoop(position, body, cond);
    }

    private FuncDef parseFunction() {
        int pos = acceptedPos;
        Token funcName = token;
        expectToken(IDENTIFIER);
        expectToken(LPAREN);
        TList<FuncDef.Parameter> params = new TList<>();
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
            params.add(new FuncDef.Parameter(p.pos, p.name(), optional));
            comma = !acceptToken(COMMA);
        }
        Stmt body = parseBody();
        return new FuncDef(pos, funcName.pos, funcName.name(), params, body, 0);
    }

    private Stmt parseBody() {
        int pos = token.pos;
        if (acceptToken(LBRACE)) return parseBlock();
        if (acceptToken(EQ)) {
            Expr expr = parseExpression();
            expectSemi();
            return new Discarded(expr.pos, expr);
        }
        reportError(pos, "Illegal function body");
        return null;
    }

    private TList<Stmt> parseForInit() {
        TList<Stmt> init = new TList<>();
        if (matchesToken(IDENTIFIER) && tokenizer.matches(COLEQ)) {
            Token id = token;
            nextToken();
            nextToken();
            init.add(new VarDef(id.pos,
                        TList.of(new VarDef.Definition(
                                id.pos, id.name(), parseExpression()
                        ))
                ));
            expectToken(SEMI);
        } else if (acceptToken(VAR)) {
            init.add(parseVar());
        } else {
            try {
                TList<Expr> list = parseExpressions();
                list.forEach((Consumer<? super Expr>) expr -> init.add(new Discarded(expr.pos, expr)));
            } catch (ParseNodeExit e) {
                report(new ParseNodeExit(e.pos, "invalid statement"));
            }
            expectToken(SEMI);
        }

        return init;
    }

    private Stmt parseFor() {
        int position = acceptedPos;
        boolean parens = acceptToken(LPAREN);
        TList<Stmt> init = acceptToken(SEMI) ? TList.empty() : parseForInit();

        Expr cond = null;

        if (!acceptToken(SEMI)) {
            cond = parseExpression();
            expectToken(SEMI);
        }
        TList<Expr> step = TList.empty();

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
        TList<Stmt> statements = new TList<>();

        while (!acceptToken(RBRACE)) {
            if (acceptToken(EOF)) {
                expectToken(RBRACE);
            }
            try {
                statements.add(parseStatement());
            } catch (ParseNodeExit e) {
                report(e);
            }
        }
        return new Block(pos, statements);
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
        expectSemi();
        return new Return(pos, expr);
    }

    private Stmt parseUnusedExpression() {
        int position = acceptedPos;
        Expr expr = parseExpression();
        expectSemi();
        return new Discarded(position, expr);
    }

    public Expr parseExpression() {
        return parseAssignment();
    }

    private TList<Expr> parseExpressions() {
        TList<Expr> expressions = new TList<>();

        do {
            expressions.add(parseExpression());
        } while (acceptToken(COMMA));

        return expressions;
    }

    Expr parseAssignment() {
        Expr expr = parseConditional();
        int pos = token.pos;

        if (acceptToken(EQ)) {
            return new Assign(pos, expr, parseAssignment());
        }

        if (isEnhancedAsgOperator(token.type)) {
            Tag tag = getAsgTag(token.type);
            nextToken();
            return new EnhancedAssign(pos, tag, expr, parseAssignment());
        }

        return expr;
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

        while (isBinaryOperator(token.type)) {
            int pos = acceptedPos;
            Tag tag = getBinOpTag(token.type);
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

    Expr parseUnary() {
        int pos = token.pos;

        if (isUnaryOperator(token.type)) {
            Tag tag = getUnaryOpTag(token.type);
            nextToken();
            return new UnaryOp(pos, tag, parseUnary());
        }

        return parsePost();
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
            TList<Invocation.Argument> args = parseInvocationArgs();
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
            expr = new Index(pos, expr, index);
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

    private TList<Invocation.Argument> parseInvocationArgs() {
        TList<Invocation.Argument> args = new TList<>();
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
            args.add(new Invocation.Argument(name.pos, name.name(), expr));
            comma = !acceptToken(COMMA);
        }

        return args;
    }

    private Expr parseInvocation(Token token) {
        TList<Invocation.Argument> args = parseInvocationArgs();
        expectToken(RPAREN);
        return new Invocation(token.pos,
                new Member(token.pos, Tag.MEMACCESS, null, token.pos, token.name()),
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
        TList<Expr> entries = new TList<>();
        if (!acceptToken(RBRACKET)) {
            do {
                entries.add(parseExpression());
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
        return new ListLiteral(pos, entries);
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

    private void unexpected(Token foundToken, java.util.List<TokenType> expectedTypes) {
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
