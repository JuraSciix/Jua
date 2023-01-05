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
        final HashSet<String> definedVars = new HashSet<>();

        /** Переменные, которые гарантированно будут доступны во внешнем скоупе */
        final HashSet<String> globalVars = new HashSet<>();

        /** Есть вероятность, что анализируемая часть дерева не выполнится */
        boolean maybeInterrupted = false;

        /** Код в этом участке никогда не выполнится */
        boolean dead = false;
    }

    ScopeState scopeState = new ScopeState() /* root-scope */;

    @Override
    public void visitConstDef(ConstDef tree) { Assert.error(); }

    @Override
    public void visitFuncDef(FuncDef tree) {
        for (FuncDef.Parameter param : tree.params) {
            // На этом этапе нет смысла добавлять переменные в globalVars
            scopeState.definedVars.add(param.name.value);
        }
        scan(tree.body);
    }

    @Override
    public void visitIf(If tree) {
        scan(tree.cond);
        ScopeState thenscope = scanScoped(tree.thenbody);
        if (tree.elsebody != null) {
            ScopeState elsescope = scanScoped(tree.elsebody);
            HashSet<String> definedvarsintersection = new HashSet<>();
            Collections.intersection(Arrays.asList(thenscope.globalVars, elsescope.globalVars), definedvarsintersection);
            defineVars(definedvarsintersection);
            scopeState.maybeInterrupted |= thenscope.maybeInterrupted || elsescope.maybeInterrupted;
            scopeState.dead |= thenscope.dead && elsescope.dead;
        } else {
            scopeState.maybeInterrupted |= thenscope.maybeInterrupted;
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        scan(tree.cond);
        scanScoped(tree.body);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        ScopeState body_scope = scanScoped(tree.body);
        scan(tree.cond);
        if (isLiteralTrue(tree.cond)) {
            scopeState.dead |= body_scope.dead;
            tree._infinite = !body_scope.maybeInterrupted;
        }
        defineVars(body_scope.globalVars);
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        ScopeState body_scope = scanScoped(tree.body);
        if (isLiteralTrue(tree.cond)) {
            scopeState.dead |= body_scope.dead;
            tree._infinite = !body_scope.maybeInterrupted;
            defineVars(body_scope.globalVars);
        }
        scan(tree.step);
    }

    @Override
    public void visitSwitch(Switch tree) {
        scan(tree.expr);
        ArrayList<Set<String>> casesGlobalVars = new ArrayList<>(tree.cases.count());
        boolean dead = true;
        for (Case case_ : tree.cases) {
            scan(case_.labels);
            ScopeState case_scope = scanScoped(case_.body);
            casesGlobalVars.add(case_scope.globalVars);
            dead &= case_scope.dead;
        }
        boolean hasDefault = tree.cases.stream().anyMatch(case_ -> case_.labels == null);
        if (hasDefault) {
            HashSet<String> definedvarsintersection = new HashSet<>();
            Collections.intersection(casesGlobalVars, definedvarsintersection);
            defineVars(definedvarsintersection);
        }
        scopeState.dead |= dead;
    }

    @Override
    public void visitCase(Case tree) {
        Assert.error();
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

        if (innerVar.hasTag(Tag.VARIABLE)) {
            Var varTree = (Var) innerVar;
            defineVar(varTree.name);
        }

        scan(tree.var);
        scan(tree.expr);
    }

    private void defineVar(Name name) {
        scopeState.definedVars.add(name.value);
        if (!scopeState.maybeInterrupted) {
            scopeState.globalVars.add(name.value);
        }
    }

    private void defineVars(HashSet<String> names) {
        scopeState.definedVars.addAll(names);
        if (!scopeState.maybeInterrupted) {
            scopeState.globalVars.addAll(names);
        }
    }

    private ScopeState scanScoped(Statement body) {
        ScopeState newState = new ScopeState();
        newState.parent = scopeState;
        scopeState = newState;
        try {
            scan(body);
        } finally {
            Assert.check(scopeState == newState);
            scopeState = newState.parent;
        }
        return newState;
    }
}
