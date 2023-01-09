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

    private static class Scope {

        final Scope parent;

        /** Явно определенные досягаемые переменные */
        final HashSet<String> aVars = new HashSet<>();

        /** Переменные, которые гарантированно будут доступны во внешнем скоупе */
        final HashSet<String> bVars = new HashSet<>();

        /** Есть вероятность, что анализируемая часть дерева не выполнится */
        boolean mustBeExecuted = true;

        /** Присутствуют ли в цикле операторы {@code break, continue, fallthrough} */
        boolean hasBreaks = false;

        /** Код в этом участке никогда не выполнится */
        boolean dead = false;

        Scope(Scope parent) {
            this.parent = parent;
        }
    }

    Scope curScope = new Scope(null);

    @Override
    public void visitTernaryOp(TernaryOp tree) {
        scan(tree.cond);
        scanScoped(tree.thenexpr, false);
        scanScoped(tree.elseexpr, false);
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        switch (tree.tag) {
            case AND:
            case OR:
            case NULLCOALSC:
                scan(tree.lhs);
                scanScoped(tree.rhs, false);
                break;
            default:
                scan(tree.lhs);
                scan(tree.rhs);
        }
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        // Анализировать функции и константы не нужно.
        // Функции анализируются отдельно
        scan(tree.stats);
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        for (FuncDef.Parameter param : tree.params) {
            // На этом этапе нет смысла добавлять переменные в globalVars
            curScope.aVars.add(param.name.value);
            scan(param.expr);
        }
        scan(tree.body);
    }

    @Override
    public void visitIf(If tree) {
        scan(tree.cond);
        Scope thenScope = scanScoped(tree.thenbody);
        curScope.hasBreaks |= thenScope.hasBreaks;
        curScope.mustBeExecuted &= thenScope.mustBeExecuted;
        if (tree.elsebody != null) {
            Scope elseScope = scanScoped(tree.elsebody);
            HashSet<String> outVisibleVars = new HashSet<>();
            Collections.intersection(
                    Arrays.asList(thenScope.bVars, elseScope.bVars),
                    outVisibleVars
            );
            defineVars(outVisibleVars);
            curScope.hasBreaks |= elseScope.hasBreaks;
            curScope.mustBeExecuted &= elseScope.mustBeExecuted;
            curScope.dead |= thenScope.dead && elseScope.dead;
        }
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        scan(tree.cond);
        scanScoped(tree.body);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        Scope bodyScope = scanScoped(tree.body, true);
        defineVars(bodyScope.bVars);
        curScope.dead |= bodyScope.dead;
        curScope.mustBeExecuted &= !bodyScope.dead;
        scan(tree.cond);

        tree._infinite = isLiteralTrue(tree.cond) && !bodyScope.hasBreaks;
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        Scope loopScope = scanScoped(tree.body);

        boolean isCondTrue = (tree.cond == null) || isLiteralTrue(tree.cond);

        if (isCondTrue) {
            defineVars(loopScope.bVars);
            curScope.dead |= loopScope.dead;
            curScope.mustBeExecuted &= !loopScope.dead;
        }

        scan(tree.step);

        tree._infinite = isCondTrue && !loopScope.hasBreaks;
    }

    @Override
    public void visitSwitch(Switch tree) {
        scan(tree.expr);
        ArrayList<Set<String>> casesGlobalVars = new ArrayList<>(tree.cases.count());
        boolean dead = true;
        for (Case case_ : tree.cases) {
            scan(case_.labels);
            Scope case_scope = scanScoped(case_.body, false);
            casesGlobalVars.add(case_scope.bVars);
            dead &= case_scope.dead;
        }
        boolean hasDefault = tree.cases.stream().anyMatch(case_ -> case_.labels == null);
        if (hasDefault) {
            HashSet<String> definedvarsintersection = new HashSet<>();
            Collections.intersection(casesGlobalVars, definedvarsintersection);
            defineVars(definedvarsintersection);
        }
        curScope.dead |= dead;
    }

    @Override
    public void visitCase(Case tree) {
        Assert.error();
    }

    @Override
    public void visitBreak(Break tree) {
        curScope.hasBreaks = true;
    }

    @Override
    public void visitContinue(Continue tree) {
        curScope.hasBreaks = true;
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        curScope.hasBreaks = true;
    }

    @Override
    public void visitReturn(Return tree) {
        scan(tree.expr);
        curScope.dead = true;
    }

    @Override
    public void visitVariable(Var tree) {
        String nameString = tree.name.value;
        for (Scope scope = curScope; scope != null; scope = scope.parent) {
            if (scope.aVars.contains(nameString)) {
                tree._defined = true;
                break;
            }
        }
    }

    @Override
    public void visitAssign(Assign tree) {
        Expression inner_var = stripParens(tree.var);

        if (inner_var.hasTag(Tag.VARIABLE)) {
            scan(tree.expr);
            Var varTree = (Var) inner_var;
            defineVar(varTree.name);
        } else {
            scan(tree.var);
            scan(tree.expr);
        }
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        scan(tree.var);
        if (tree.hasTag(Tag.ASG_NULLCOALSC)) {
            scanScoped(tree.expr, false);
        } else {
            scan(tree.expr);
        }
    }

    private void defineVar(Name name) {
        curScope.aVars.add(name.value);
        if (curScope.mustBeExecuted) {
            curScope.bVars.add(name.value);
        }
    }

    private void defineVars(HashSet<String> names) {
        curScope.aVars.addAll(names);
        if (curScope.mustBeExecuted) {
            curScope.bVars.addAll(names);
        }
    }

    private Scope scanScoped(Tree tree) {
        return scanScoped(tree, false);
    }

    private Scope scanScoped(Tree tree, boolean mustBeExecuted) {
        Scope newScope = new Scope(curScope);
        newScope.mustBeExecuted = mustBeExecuted;
        curScope = newScope;
        try {
            scan(tree);
        } finally {
            Assert.check(curScope == newScope);
            curScope = newScope.parent;
        }
        return newScope;
    }
}
