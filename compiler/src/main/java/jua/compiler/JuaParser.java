package jua.compiler;

import jua.compiler.Tokens.Token;
import jua.compiler.Tokens.TokenType;
import jua.compiler.Tree.*;
import jua.utils.Conversions;
import jua.utils.List;

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

    public JuaParser(Source source, Log log) {
        this.source = source;
        this.log = log;
        tokenizer = new Lexer(source, log);
    }

    public CompilationUnit parseCompilationUnit() {
        List<Statement> stats = new List<>();
        List<FuncDef> funcDefs = new List<>();
        List<ConstDef> constDefs = new List<>();
        nextToken();

        int pos = token.pos;
        List<Import> imports = new List<>();
        while (acceptToken(USE)) {
            try {
                Token lib = token;
                expectToken(IDENTIFIER);
                expectToken(DOT);
                if (acceptToken(STAR)) {
                    imports.add(new Import(pos, lib.toName(), null));
                } else {
                    Token target = token;
                    expectToken(IDENTIFIER);
                    imports.add(new Import(pos, lib.toName(), target.toName()));
                }
                expectToken(SEMI);
            } catch (ParseNodeExit e) {
                report(e);
            }
            pos = token.pos;
        }

        while (!acceptToken(EOF)) {
            pos = token.pos;
            try {
                if (acceptToken(FN)) {
                    funcDefs.add(parseFunction(pos));
                    continue;
                }
                if (acceptToken(CONST)) {
                    constDefs.add(parseConst(pos));
                    continue;
                }
                stats.add(parseStatement());
            } catch (ParseNodeExit e) {
                report(e);
            }
        }
        return new CompilationUnit(0, source, imports, constDefs, funcDefs, stats);
    }

    private void report(ParseNodeExit e) {
        log.error(source, e.pos, e.msg);
    }

    public Statement parseStatement() {
        int pos = token.pos;

        switch (token.type) {
            case BREAK: {
                nextToken();
                return parseBreak(pos);
            }
            case CASE: {
                nextToken();
                pError(pos, "'case' is not allowed outside of switch.");
            }
            case CONST: {
                nextToken();
                pError(pos, "constant declaration is not allowed here");
            }
            case CONTINUE: {
                nextToken();
                return parseContinue(pos);
            }
            case DEFAULT: {
                nextToken();
                pError(pos, "'default' is not allowed outside of switch.");
            }
            case DO: {
                nextToken();
                return parseDo(pos);
            }
            case ELSE: {
                nextToken();
                pError(pos, "'else' is not allowed without if-statement.");
            }
            case EOF: {
                nextToken();
                pError(pos, "missing expected statement.");
            }
            case FALLTHROUGH: {
                nextToken();
                return parseFallthrough(pos);
            }
            case FN: {
                nextToken();
                pError(pos, "function declaration is not allowed here");
            }
            case FOR: {
                nextToken();
                return parseFor(pos);
            }
            case IF: {
                nextToken();
                return parseIf(pos);
            }
            case LBRACE: {
                nextToken();
                return parseBlock(pos);
            }
            case RETURN: {
                nextToken();
                return parseReturn(pos);
            }
            case SEMI: {
                nextToken();
                return new Block(pos, new List<>());
            }
            case SWITCH: {
                nextToken();
                return parseSwitch(pos);
            }
            case VAR: {
                nextToken();
                return parseVar(pos);
            }
            case WHILE: {
                nextToken();
                return new WhileLoop(pos, parseExpression(), parseStatement());
            }
            default: return parseUnusedExpression();
        }
    }

    public Expression parseExpression() {
        return parseAssignment();
    }

    private VarDef parseVar(int pos) {
        List<VarDef.Definition> defs = new List<>();
        do {
            Token name = token;
            expectToken(IDENTIFIER);
            Expression init = null;
            if (acceptToken(EQ)) {
                init = parseExpression();
            }
            defs.add(new VarDef.Definition(name.toName(), init));
        } while (acceptToken(COMMA));
        expectToken(SEMI);
        return new VarDef(pos, defs);
    }

    private Statement parseBreak(int position) {
        expectToken(SEMI);
        return new Break(position);
    }

    private ConstDef parseConst(int position) {
        List<ConstDef.Definition> definitions = new List<>();

        do {
            try {
                Token name = token;
                expectToken(IDENTIFIER);
                expectToken(EQ);
                definitions.add(new ConstDef.Definition(name.toName(), parseExpression()));
            } catch (ParseNodeExit e) {
                report(e);
            }
        } while (acceptToken(COMMA));

        expectToken(SEMI);
        return new ConstDef(position, definitions);
    }

    private Statement parseContinue(int position) {
        expectToken(SEMI);
        return new Continue(position);
    }

    private Statement parseDo(int position) {
        Statement body = parseStatement();
        expectToken(WHILE);
        Expression cond = parseExpression();
        expectToken(SEMI);
        return new DoLoop(position, body, cond);
    }

    private Statement parseFallthrough(int position) {
        expectToken(SEMI);
        return new Fallthrough(position);
    }

    public FuncDef parseFunctionDeclare() {
        int pos = token.pos;
        expectToken(FN);
        return parseFunction(pos);
    }

    private FuncDef parseFunction(int pos) {
        Name funcName = token.toName();
        expectToken(IDENTIFIER);
        expectToken(LPAREN);
        List<FuncDef.Parameter> params = new List<>();
        boolean comma = false, optionalState = false;

        while (!acceptToken(RPAREN)) {
            if (acceptToken(EOF) || comma && !acceptToken(COMMA)) {
                expectToken(RPAREN);
            }
            Token name0 = token;
            expectToken(IDENTIFIER);
            Name name1 = name0.toName();
            Expression optional = null;

            if (acceptToken(EQ)) {
                optional = parseExpression();
                optionalState = true;
            } else if (optionalState) {
                pError(name0.pos, "here must be a optional argument.");
            }
            params.add(new FuncDef.Parameter(name1, optional));
            comma = !acceptToken(COMMA);
        }
        Statement body = parseBody();
        return new FuncDef(pos, funcName, params, body);
    }

    private Statement parseBody() {
        int pos = token.pos;
        if (acceptToken(LBRACE)) return parseBlock(pos);
        if (acceptToken(EQ)) {
            Expression expr = parseExpression();
            expectToken(SEMI);
            return new Discarded(expr.pos, expr);
        }
        pError(pos, "Illegal function body");
        return null;
    }

    private List<Statement> parseStatements() {
        List<Statement> expressions = new List<>();

        do {
            expressions.add(parseStatement());
        } while (acceptToken(COMMA));

        return expressions;
    }

    private List<Statement> parseForInit() {
        List<Statement> init = List.empty();
        int pos = token.pos;
        if (acceptToken(VAR)) {
            init.add(parseVar(pos));
        } else {
            try {
                init.addAll(parseExpressions().map(expr -> new Discarded(expr.pos, expr)));
            } catch (ParseNodeExit e) {
                report(new ParseNodeExit(e.pos, "invalid statement"));
            }
            expectToken(SEMI);
        }

        return init;
    }

    private Statement parseFor(int position) {
        boolean parens = acceptToken(LPAREN);
        List<Statement> init = acceptToken(SEMI) ? List.empty() : parseForInit();

        Expression cond = null;

        if (!acceptToken(SEMI)) {
            cond = parseExpression();
            expectToken(SEMI);
        }
        List<Discarded> step = null;

        if (parens) {
            if (!acceptToken(RPAREN)) {
                step = parseExpressions().map(expr -> new Discarded(expr.pos, expr));
                expectToken(RPAREN);
            }
        } else {
            step = parseExpressions().map(expr -> new Discarded(expr.pos, expr));
        }
        return new ForLoop(position, init, cond, step, parseStatement());
    }

    private Statement parseIf(int position) {
        Expression cond = parseExpression();
        Statement body = parseStatement();

        if (!acceptToken(ELSE)) {
            return new If(position, cond, body, null);
        }
        return new If(position, cond, body, parseStatement());
    }

    private Statement parseBlock(int position) {
        List<Statement> statements = new List<>();

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
        return new Block(position, statements);
    }

    private Statement parseReturn(int position) {
        if (acceptToken(SEMI)) {
            return new Return(position, null);
        }
        Expression expr = null;
        try {
            expr = parseExpression();
        } catch (ParseNodeExit e) {
            report(e);
        }
        expectToken(SEMI);
        return new Return(position, expr);
    }

    private Statement parseSwitch(int position) {
        Expression selector = parseExpression();
        List<Case> cases = new List<>();
        expectToken(LBRACE);

        while (!acceptToken(RBRACE)) {
            int position1 = token.pos;

            if (acceptToken(CASE)) {
                try {
                    cases.add(parseCase(position1, false));
                } catch (ParseNodeExit e) {
                    report(e);
                }
            } else if (acceptToken(DEFAULT)) {
                try {
                    cases.add(parseCase(position1, true));
                } catch (ParseNodeExit e) {
                    report(e);
                }
            } else if (acceptToken(EOF)) {
                expectToken(RBRACE);
            } else {
                unexpected(token, List.of(CASE, DEFAULT, RBRACE));
            }
        }
        return new Switch(position, selector, cases);
    }

    private Case parseCase(int position, boolean isDefault) {
        List<Expression> expressions = null;

        if (!isDefault) {
            expressions = parseExpressions();
        }
        expectToken(COL);
        Statement body = parseStatement();
        return new Case(position, expressions, body);
    }

    private Statement parseUnusedExpression() {
        int position = token.pos;
        Expression expr = parseExpression();
        expectToken(SEMI);
        return new Discarded(position, expr);
    }

    private List<Expression> parseExpressions() {
        List<Expression> expressions = new List<>();

        do {
            expressions.add(parseExpression());
        } while (acceptToken(COMMA));

        return expressions;
    }

    private Expression parseAssignment() {
        Expression expr = parseCoalesce();
        int position = token.pos;

        switch (token.type) {
            case AMPEQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_BIT_AND, expr, parseAssignment());
            }
            case BAREQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_BIT_OR, expr, parseAssignment());
            }
            case CARETEQ: {
                nextToken();
                return new CompoundAssign(position, Tag.BIT_XOR, expr, parseAssignment());
            }
            case EQ: {
                nextToken();
                return new Assign(position, expr, parseAssignment());
            }
            case GTGTEQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_SR, expr, parseAssignment());
            }
            case LTLTEQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_SL, expr, parseAssignment());
            }
            case MINUSEQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_SUB, expr, parseAssignment());
            }
            case PERCENTEQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_REM, expr, parseAssignment());
            }
            case PLUSEQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_ADD, expr, parseAssignment());
            }
            case QUESQUESEQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_COALESCE, expr, parseAssignment());
            }
            case SLASHEQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_DIV, expr, parseAssignment());
            }
            case STAREQ: {
                nextToken();
                return new CompoundAssign(position, Tag.ASG_MUL, expr, parseAssignment());
            }
            default: return expr;
        }
    }

    private Expression parseCoalesce() {
        Expression expr = parseTernary();
        int pos = token.pos;

        if (acceptToken(QUESQUES)) {
            expr = new BinaryOp(pos, Tag.COALESCE, expr, parseCoalesce());
        }
        return expr;
    }

    private Expression parseTernary() {
        Expression expr = parseOr();

        while (true) {
            int position = token.pos;

            if (acceptToken(QUES)) {
                expr = parseTernary0(position, expr);
            } else {
                return expr;
            }
        }
    }

    private Expression parseTernary0(int position, Expression cond) {
        Expression right = parseExpression();
        expectToken(COL);
        return new TernaryOp(position, cond, right, parseExpression());
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
        Expression expr = parseConditional();

        while (true) {
            int position = token.pos;

            if (acceptToken(EQEQ)) {
                expr = new BinaryOp(position, Tag.EQ, expr, parseConditional());
            } else if (acceptToken(BANGEQ)) {
                expr = new BinaryOp(position, Tag.NE, expr, parseConditional());
            } else {
                return expr;
            }
        }
    }

    private Expression parseConditional() {
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
        Expression expr = parseUnary();

        while (true) {
            int position = token.pos;

            if (acceptToken(PERCENT)) {
                expr = new BinaryOp(position, Tag.REM, expr, parseUnary());
            } else if (acceptToken(SLASH)) {
                expr = new BinaryOp(position, Tag.DIV, expr, parseUnary());
            } else if (acceptToken(STAR)) {
                expr = new BinaryOp(position, Tag.MUL, expr, parseUnary());
            } else {
                return expr;
            }
        }
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
            List<Invocation.Argument> args = parseInvocationArgs();
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
                            expr, member.toName());
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
        Token token = this.token;
        nextToken();

        switch (token.type) {
            case EOF: {
                pError(token.pos, "missing expected expression.");
            }
            case FALSE: {
                return new Literal(token.pos, false);
            }
            case FLOATLITERAL: {
                return parseFloat(token);
            }
            case IDENTIFIER: {
                return parseIdentifier(token);
            }
            case INTLITERAL: {
                return parseInt(token);
            }
            case LBRACE: {
                return parseMapInit(token.pos);
            }
            case LBRACKET: {
                return parseListInit(token.pos);
            }
            case LPAREN: {
                return parseParens(token.pos);
            }
            case NULL: {
                return new Literal(token.pos, null);
            }
            case STRINGLITERAL: {
                return new Literal(token.pos, token.value());
            }
            case TRUE: {
                return new Literal(token.pos, true);
            }
            default:
                unexpected(token, List.of(
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
            pError(token.pos, "number too large.");
        }
        return new Literal(token.pos, d);
    }

    private Expression parseIdentifier(Token token) {
        if (acceptToken(LPAREN)) {
            return parseInvocation(token);
        }
        return new Var(token.pos, token.toName());
    }

    private List<Invocation.Argument> parseInvocationArgs() {
        List<Invocation.Argument> args = new List<>();
        boolean comma = false;

        while (!matchesToken(RPAREN)) {
            if (acceptToken(EOF) || comma && !acceptToken(COMMA)) {
                expectToken(RPAREN);
            }
            Name name = null;
            Expression expr;
            if (matchesToken(IDENTIFIER)) {
                Token t = token;
                expr = parseExpression();
                if (expr.hasTag(Tag.VAR) && acceptToken(COL)) {
                    name = t.toName();
                    expr = parseExpression();
                }
            } else {
                expr = parseExpression();
            }
            args.add(new Invocation.Argument(name, expr));
            comma = !acceptToken(COMMA);
        }

        return args;
    }

    private Expression parseInvocation(Token token) {
        List<Invocation.Argument> args = parseInvocationArgs();
        expectToken(RPAREN);
        return new Invocation(token.pos,
                new MemberAccess(token.pos, Tag.MEMACCESS, null, token.toName()),
                args);
    }

    private Expression parseInt(Token token) {
        try {
            long value = Conversions.parseLong(token.value(), token.radix());
            return new Literal(token.pos, value);
        } catch (NumberFormatException e) {
            pError(token.pos, "number too large.");
            return null;
        }
    }

    private ListLiteral parseListInit(int pos) {
        List<Expression> entries = new List<>();
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
                unexpected(token, List.of(COMMA, RBRACKET));
            } while (true);
        }
        return new ListLiteral(pos, entries);
    }

    private MapLiteral parseMapInit(int pos) {
        List<MapLiteral.Entry> entries = new List<>();
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
                            unexpected(token, List.of(FLOATLITERAL, INTLITERAL, IDENTIFIER));
                            throw new AssertionError(); // UNREACHABLE
                    }
                    nextToken();
                    field = true;
                }
                expectToken(COL);
                Expression value = parseExpression();
                MapLiteral.Entry entry = new MapLiteral.Entry(entryPos, key, value);
                entry.field = field;
                entries.add(entry);
                if (acceptToken(COMMA)) {
                    if (acceptToken(RBRACE)) {
                        break;
                    }
                    continue;
                }
                if (acceptToken(RBRACE)) {
                    break;
                }
                unexpected(token, List.of(COMMA, RBRACE));
            } while (true);
        }
        return new MapLiteral(pos, entries);
    }

    private Expression parseParens(int position) {
        Expression expr = parseExpression();
        expectToken(RPAREN);
        return new Parens(position, expr);
    }

    private void nextToken() {
        token = tokenizer.nextToken();
    }

    private boolean matchesToken(TokenType type) {
        return token.type == type;
    }
    
    private boolean acceptToken(TokenType type) {
        if (matchesToken(type)) {
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
                pError(token.pos, expected + " expected, " + found + " found.");
                return;
            }

            if (expected != null) {
                pError(token.pos, expected + " expected.");
                return;
            }

            if (found != null) {
                pError(token.pos, "unexpected " + found + ".");
                return;
            }

            pError(token.pos, "invalid syntax.");
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

    private void unexpected(Token token) {
        pError(token.pos, "unexpected " + token2string(token) + '.');
    }

    private void unexpected(Token foundToken, List<TokenType> expectedTypes) {
        String expects = expectedTypes
                .stream()
                .map(JuaParser::type2string)
                .collect(Collectors.joining(", "));
        pError(foundToken.pos, "unexpected " + token2string(foundToken) + ", expected instead: " + expects);
    }

    private void pError(int position, String message) {
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
