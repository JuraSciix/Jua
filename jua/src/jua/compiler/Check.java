package jua.compiler;

import jua.compiler.Tree.*;
import jua.compiler.Tree.Scanner;
import jua.util.Assert;
import jua.util.Collections;

import java.util.*;

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

    ScopeState scopeState = new ScopeState() /* root-scope */, lastScopeState;

    /**
     * Скоуп - контекст тела цикла, условия, кейза
     */
    static class ScopeState {

        ScopeState parent;

        /** Явно определенные досягаемые переменные */
        final Set<String> definedVars = new HashSet<>();

        /** Переменные, которые упоминались где-либо (включая другие скоупы) */
        final Set<String> knownVars = new HashSet<>();

        /** Есть вероятность, что анализируемая часть дерева не выполнится */
        boolean maybeInterrupted = false;

        /** Код никогда не выполнится */
        boolean dead = false;
    }

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

        ScopeState rootScopeState = scopeState;
        try {
            scopeState = new ScopeState(); // У функции свой, отдельный root-scope.
            for (FuncDef.Parameter param : tree.params) {
                name = param.name;
                if (!scopeState.knownVars.add(name.value)) {
                    log.error(name.pos, "duplicate parameter");
                    continue;
                }
                scopeState.definedVars.add(name.value);
                if (param.expr != null) {
                    Expression expr = stripParens(param.expr);
                    if (!isLiteral(expr)) {
                        log.error(expr.pos, "only literals are allowed as the default parameter value expression");
                    }
                }
            }

            scan(tree.body);
        } finally {
            scopeState = rootScopeState;
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
    public void visitIf(If tree) {
        scan(tree.cond);
        scanScoped(tree.thenbody);
        ScopeState thenscope = lastScopeState;
        if (tree.elsebody != null) {
            scanScoped(tree.elsebody);
            ScopeState elsescope = lastScopeState;
            scopeState.knownVars.addAll(thenscope.knownVars);
            scopeState.knownVars.addAll(elsescope.knownVars);
            Set<String> definedvarsintersection = new HashSet<>();
            Collections.intersection(Arrays.asList(thenscope.definedVars, elsescope.definedVars), definedvarsintersection);
            scopeState.definedVars.addAll(definedvarsintersection);
            scopeState.maybeInterrupted = thenscope.maybeInterrupted || elsescope.maybeInterrupted;
        } else {
            scopeState.knownVars.addAll(thenscope.knownVars);
            scopeState.maybeInterrupted = thenscope.maybeInterrupted;
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        scan(tree.cond);
        scanLoopBody(tree.body, true);
    }

    @Override
    public void visitSwitch(Switch tree) {
        scan(tree.expr);
        ArrayList<Set<String>> casedDefinedVars = new ArrayList<>(tree.cases.size());
        boolean dead = true;
        for (Case case_ : tree.cases) {
            scan(case_);
            casedDefinedVars.add(lastScopeState.definedVars);
            if (!lastScopeState.dead) dead = false;
        }
        Set<String> definedvarsintersection = new HashSet<>();
        Collections.intersection(casedDefinedVars, definedvarsintersection);
        scopeState.definedVars.addAll(definedvarsintersection);
        if (dead) scopeState.dead = true;
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        scanLoopBody(tree.body, false);
        scan(tree.cond);
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        scan(tree.step);
        scanLoopBody(tree.body, true);
    }

    private void scanLoopBody(Statement body, boolean separateScope) {
        int prevConditions = conditions;
        try {
            conditions |= C_ALLOW_BREAK;
            conditions |= C_ALLOW_CONTINUE;
            if (separateScope) {
                scanScoped(body);

                scopeState.knownVars.addAll(lastScopeState.knownVars);
            } else {
                scan(body);
                lastScopeState = scopeState;
            }
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
            scanScoped(caseBodyStatement);
        } finally {
            conditions = prevConditions;
        }
    }

    private void scanScoped(Statement body) {
        ScopeState newState = new ScopeState();
        newState.parent = scopeState;
        scopeState = newState;
        try {
            scan(body);
        } finally {
            Assert.check(scopeState == newState);
            scopeState = newState.parent;
            lastScopeState = newState;
        }
    }

    @Override
    public void visitBreak(Break tree) {
        if ((conditions & C_ALLOW_BREAK) == 0) {
            log.error(tree.pos, "break-statement is allowed only inside loop/switch-case");
        } else {
            scopeState.maybeInterrupted = true;
        }
    }

    @Override
    public void visitContinue(Continue tree) {
        if ((conditions & C_ALLOW_CONTINUE) == 0) {
            log.error(tree.pos, "continue-statement is allowed only inside loop");
        } else {
            scopeState.maybeInterrupted = true;
        }
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        if ((conditions & C_ALLOW_FALLTHROUGH) == 0) {
            log.error(tree.pos, "fallthrough-statement is allowed only inside switch-case");
        } else {
            scopeState.maybeInterrupted = true;
        }
    }

    @Override
    public void visitVariable(Var tree) {
        Name name = tree.name;
        for (ScopeState scope = scopeState; scope != null; scope = scope.parent) {
            if (scope.knownVars.contains(name.value)) {
                tree.definitelyExists = scope.definedVars.contains(name.value);
                return;
            }
        }

        log.error(name.pos, "attempt to refer to an undefined variable");

        // Регистрируем переменную, чтобы идентичной ошибки больше не возникало
        ScopeState upperScope = scopeState;
        while (upperScope.parent != null) upperScope = upperScope.parent;
        upperScope.knownVars.add(name.value);
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
    public void visitReturn(Return tree) {
        scan(tree.expr);
        scopeState.dead = true;
    }

    @Override
    public void visitAssign(Assign tree) {
        Expression innerVar = stripParens(tree.var);

        if (!isAssignable(innerVar)) {
            log.error(innerVar.pos, "attempt to assign a value to a non-accessible expression");
        } else if (innerVar.hasTag(Tag.VARIABLE)) {
            Var varTree = (Var) innerVar;
            scopeState.knownVars.add(varTree.name.value);
            if (!scopeState.maybeInterrupted) {
                scopeState.definedVars.add(varTree.name.value);
            }
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
