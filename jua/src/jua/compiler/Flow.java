package jua.compiler;

import jua.compiler.Tree.*;
import jua.util.Assert;
import jua.util.Collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static jua.compiler.TreeInfo.isLiteralTrue;
import static jua.compiler.TreeInfo.stripParens;

/**
 * Анализ кода на достижимость участков, видимость переменных.
 * Следует после свертки констант и семантических проверок.
 */
public class Flow extends Scanner {

    /**
     * Скоуп - контекст тела цикла, условия, кейза
     */
    static class ScopeState {

        ScopeState parent;

        /** Явно определенные досягаемые переменные */
        final Set<String> definedVars = new HashSet<>();

        /** Есть вероятность, что анализируемая часть дерева не выполнится */
        boolean maybeInterrupted = false;

        /** Код в этом участке никогда не выполнится */
        boolean dead = false;
    }

    ScopeState scopeState = new ScopeState() /* root-scope */, lastScopeState;

    @Override
    public void visitConstDef(ConstDef tree) { Assert.error(); }

    @Override
    public void visitFuncDef(FuncDef tree) {
        for (FuncDef.Parameter param : tree.params) {
            scopeState.definedVars.add(param.name.value);
        }
        scan(tree.body);
    }

    @Override
    public void visitIf(If tree) {
        scan(tree.cond);
        scanScoped(tree.thenbody);
        ScopeState thenscope = lastScopeState;
        if (tree.elsebody != null) {
            scanScoped(tree.elsebody);
            ScopeState elsescope = lastScopeState;
            Set<String> definedvarsintersection = new HashSet<>();
            Collections.intersection(Arrays.asList(thenscope.definedVars, elsescope.definedVars), definedvarsintersection);
            scopeState.definedVars.addAll(definedvarsintersection);
            scopeState.maybeInterrupted |= thenscope.maybeInterrupted || elsescope.maybeInterrupted;
            scopeState.dead |= thenscope.dead && elsescope.dead;
        } else {
            scopeState.maybeInterrupted |= thenscope.maybeInterrupted;
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        scan(tree.cond);
        scanLoopBody(tree.body, true);
        if (isLiteralTrue(tree.cond)) {
            scopeState.dead |= lastScopeState.dead;
            tree._infinite = !lastScopeState.maybeInterrupted;
        }
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        scanLoopBody(tree.body, false);
        scan(tree.cond);
        if (isLiteralTrue(tree.cond)) {
            scopeState.dead |= lastScopeState.dead;
            tree._infinite = !lastScopeState.maybeInterrupted;
        }
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        scanLoopBody(tree.body, true);
        if (isLiteralTrue(tree.cond)) {
            scopeState.dead |= lastScopeState.dead;
            tree._infinite = !lastScopeState.maybeInterrupted;
        }
        scan(tree.step);
    }

    private void scanLoopBody(Statement body, boolean separateScope) {
        if (separateScope) {
            scanScoped(body);
        } else {
            scan(body);
            lastScopeState = scopeState;
        }
    }

    @Override
    public void visitSwitch(Switch tree) {
        scan(tree.expr);
        ArrayList<Set<String>> casedDefinedVars = new ArrayList<>(tree.cases.size());
        boolean dead = true;
        for (Case case_ : tree.cases) {
            scan(case_);
            casedDefinedVars.add(lastScopeState.definedVars);
            dead &= lastScopeState.dead;
        }
        boolean hasDefault = tree.cases.stream().anyMatch(case_ -> case_.labels == null);
        if (hasDefault) {
            Set<String> definedvarsintersection = new HashSet<>();
            Collections.intersection(casedDefinedVars, definedvarsintersection);
            scopeState.definedVars.addAll(definedvarsintersection);
        }
        scopeState.dead |= dead;
    }

    @Override
    public void visitCase(Case tree) {
        scan(tree.labels);
        scanCaseBody(tree.body);
    }

    private void scanCaseBody(Statement caseBodyStatement) {
        scanScoped(caseBodyStatement);
    }

    @Override
    public void visitBreak(Break tree) {
        scopeState.maybeInterrupted = true;
    }

    @Override
    public void visitContinue(Continue tree) {
        scopeState.maybeInterrupted = true;
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        scopeState.maybeInterrupted = true;
    }

    @Override
    public void visitReturn(Return tree) {
        scan(tree.expr);
        scopeState.dead = true;
    }

    @Override
    public void visitVariable(Var tree) {
        Name name = tree.name;
        for (ScopeState scope = scopeState; scope != null; scope = scope.parent) {
            if (scope.definedVars.contains(name.value)) {
                tree._defined = true;
                break;
            }
        }
    }

    @Override
    public void visitAssign(Assign tree) {
        Expression innerVar = stripParens(tree.var);

        if (innerVar.hasTag(Tag.VARIABLE) && !scopeState.maybeInterrupted) {
            Var varTree = (Var) innerVar;
            scopeState.definedVars.add(varTree.name.value);
        }

        scan(tree.var);
        scan(tree.expr);
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
}
