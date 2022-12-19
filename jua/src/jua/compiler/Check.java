package jua.compiler;

import jua.compiler.Tree.*;

import java.util.HashSet;
import java.util.Set;

import static jua.compiler.TreeInfo.*;

public class Check extends Scanner {

    /** Разрешается определять функции и константы. */
    private static final int C_ALLOW_DECL = 0x1;
    /** Разрешается использовать оператор {@code break}. */
    private static final int C_ALLOW_BREAK = 0x2;
    /** Разрешается использовать оператор {@code continue}. */
    private static final int C_ALLOW_CONTINUE = 0x4;
    /** Разрешается использовать оператор {@code fallthrough}. */
    private static final int C_ALLOW_FALLTHROUGH = 0x8;

    final Set<String> functions = new HashSet<>();

    final Set<String> constants = new HashSet<>();

    Log log;

    int conditions;

    Set<String> vars = new HashSet<>();

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        log = tree.source.getLog();
        conditions |= C_ALLOW_DECL;
        scan(tree.stats);
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        if ((conditions & C_ALLOW_DECL) == 0) {
            log.error(tree.pos, "constant declaration is not allowed here");
            return;
        }

        for (ConstDef.Definition def : tree.defs) {
            Name name = def.name;
            if (!constants.add(name.value)) {
                log.error(name.pos, "duplicate constant");
                continue;
            }

            Expression expr = stripParens(def.expr);
            if (!expr.hasTag(Tag.LITERAL) && !expr.hasTag(Tag.ARRAYLITERAL)) {
                log.error(expr.pos, "only literals are allowed as the constant value expression");
            }
        }
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        if ((conditions & C_ALLOW_DECL) == 0) {
            log.error(tree.pos, "function declaration is not allowed here");
            return;
        }

        // Делаем проверку заранее, чтобы избежать аллокаций.
        Name name = tree.name;
        if (!functions.add(name.value)) {
            log.error(name.pos, "duplicate function");
            return;
        }

        Set<String> prevVars = vars;
        try {
            vars = new HashSet<>();

            for (FuncDef.Parameter param : tree.params) {
                name = param.name;
                if (!vars.add(name.value)) {
                    log.error(name.pos, "duplicate parameter");
                    continue;
                }
                if (param.expr != null) {
                    Expression expr = stripParens(param.expr);
                    if (!isLiteral(expr)) {
                        log.error(expr.pos, "only literals are allowed as the default parameter value expression");
                    }
                }
            }

            scan(tree.body);
        } finally {
            vars = prevVars;
        }
    }

    @Override
    public void visitBlock(Block tree) {
        int prevConditions = conditions;
        try {
            conditions &= ~C_ALLOW_DECL;
            scan(tree.stats);
        } finally {
            conditions = prevConditions;
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        scan(tree.cond);
        scanLoopBody(tree.body);
    }

    @Override
    public void visitFor(ForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        scan(tree.step);
        scanLoopBody(tree.body);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        scanLoopBody(tree.body);
        scan(tree.cond);
    }

    private void scanLoopBody(Statement loopBodyStatement) {
        int prevConditions = conditions;
        try {
            conditions |= C_ALLOW_BREAK;
            conditions |= C_ALLOW_CONTINUE;
            scan(loopBodyStatement);
        } finally {
            conditions = prevConditions;
        }
    }

    @Override
    public void visitCase(Case tree) {
        if (tree.labels != null) { // default-case check
            for (Expression label : tree.labels) {
                Expression innerLabel = stripParens(label);
                if (!isLiteral(innerLabel)) {
                    log.error(innerLabel.pos, "only literals are allowed as the case label expression");
                }
            }
        }
        scanCaseBody(tree.body);
    }

    private void scanCaseBody(Statement caseBodyStatement) {
        int prevConditions = conditions;
        try {
            conditions |= C_ALLOW_BREAK;
            conditions |= C_ALLOW_FALLTHROUGH;
            scan(caseBodyStatement);
        } finally {
            conditions = prevConditions;
        }
    }

    @Override
    public void visitBreak(Break tree) {
        if ((conditions & C_ALLOW_BREAK) == 0) {
            log.error(tree.pos, "break-statement is allowed only inside loop/switch-case");
        }
    }

    @Override
    public void visitContinue(Continue tree) {
        if ((conditions & C_ALLOW_CONTINUE) == 0) {
            log.error(tree.pos, "continue-statement is allowed only inside loop");
        }
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        if ((conditions & C_ALLOW_FALLTHROUGH) == 0) {
            log.error(tree.pos, "fallthrough-statement is allowed only inside switch-case");
        }
    }

    @Override
    public void visitVariable(Var tree) {
        Name name = tree.name;
        if (!vars.contains(name.value) && !constants.contains(name.value)) {
            log.error(name.pos, "attempt to refer to an undefined variable");
            // Регистрируем переменную, чтобы идентичной ошибки больше не возникало
            vars.add(name.value);
        }
    }

    @Override
    public void visitInvocation(Invocation tree) {
        // Я не вызываю ниже stripParens потому что так надо
        Expression callee = tree.callee;
        if (!callee.hasTag(Tag.MEMACCESS) || ((MemberAccess) callee).expr != null) {
            log.error(stripParens(callee).pos, "only function calls are allowed");
            return;
        }

        MemberAccess calleeTree = (MemberAccess) callee;

        if (calleeTree.member.value.equals("length")) {
            if (tree.args.size() != 1) {
                log.error(tree.pos, "the function 'length' takes a single parameter");
                return;
            }
        } else {
            if (tree.args.size() > 255) {
                log.error(tree.pos, "the number of call arguments cannot exceed 255");
                return;
            }
        }

        for (Invocation.Argument a : tree.args) {
            Name name = a.name;
            if (name != null) {
                log.error(name.pos, "named arguments not yet supported");
                continue;
            }
            scan(a.expr);
        }
    }

    @Override
    public void visitAssign(Assign tree) {
        Expression innerVar = stripParens(tree.var);

        if (!isAssignable(innerVar)) {
            log.error(innerVar.pos, "attempt to assign a value to a non-accessible expression");
        } else if (innerVar.hasTag(Tag.VARIABLE)) {
            Var varTree = (Var) innerVar;
            vars.add(varTree.name.value);
        } else {
            scan(tree.var);
        }

        scan(tree.expr);
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        Expression innerVar = stripParens(tree.dst);

        if (!isAssignable(innerVar)) {
            log.error(innerVar.pos, "attempt to assign a value to a non-accessible expression");
        } else {
            scan(tree.dst);
        }
        scan(tree.src);
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        switch (tree.getTag()) {
            case POSTINC: case POSTDEC:
            case PREINC: case PREDEC:
                Expression expr = stripParens(tree.expr);
                if (!isAssignable(expr)) {
                    log.error(expr.pos, "the increment operation is allowed only on accessible expressions");
                    break;
                }
        }
        scan(tree.expr);
    }
}
