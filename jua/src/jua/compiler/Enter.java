package jua.compiler;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import jua.compiler.Tree.*;
import jua.utils.Assert;
import jua.utils.List;

/**
 * Учет локальных переменных
 */
public class Enter extends Scanner {

    private class Scope {

        /** Родительская (внешняя) область. */
        final Scope parent;

        /** Соотнесение названий локальных переменных к их номерам. */
        final Object2IntArrayMap<String> localIds = new Object2IntArrayMap<>();

        Scope(Scope parent) {
            this.parent = parent;
        }

        /** Декларирует переменную, проверя дубликаты. */
        void declare(Name local) {
            if (globalScope.isConstantDefined(local)) {
                log.error(local.pos, "constant assignment");
                return;
            }
            int nextId = 0;
            for (Scope scope = this; scope != null; scope = scope.parent) {
                if (scope.localIds.containsKey(local.value)) {
                    log.error(local.pos, "variable redeclaration");
                    return;
                }
                nextId += scope.localIds.size();
            }
            localIds.put(local.value, local.id = nextId);
        }

        /** Ищет переменную, проверяя ее существование. */
        void lookup(Name local) {
            if (globalScope.isConstantDefined(local)) return;
            int nextId = 0;
            for (Scope scope = this; scope != null; scope = scope.parent) {
                if (scope.localIds.containsKey(local.value)) {
                    local.id = scope.localIds.getInt(local.value);
                    return;
                }
                nextId += scope.localIds.size();
            }
            log.error(local.pos, "undeclared variable");
            localIds.put(local.value, local.id = nextId);
        }

    }

    private final ProgramScope globalScope;

    /** Журнал ошибок. */
    private Log log;

    /** Текущая область видимости. */
    private Scope scope;

    public Enter(ProgramScope globalScope) {
        this.globalScope = globalScope;
    }

    private void ensureRootScope() {
        Assert.ensure(scope == null, "non-root scope");
    }

    private void ensureScopeChainUnaffected(Scope parent) {
        Assert.ensure(scope.parent == parent, "scope chain has been affected");
    }

    private void checkNotVarDef(Tree tree) {
        if (tree.hasTag(Tag.VARDEF)) {
            log.error(tree.pos, "variable declaration isn't allowed here");
        }
    }

    private Scope scanInnerScope(Scope parentScope, List<? extends Tree> trees) {
        Scope innerScope = new Scope(parentScope);
        if (trees.nonEmpty()) {
            scope = innerScope;
            scan(trees);
            ensureScopeChainUnaffected(parentScope);
            scope = parentScope;
        }
        return innerScope;
    }

    private void scanBody(Scope parentScope, Tree tree) {
        if (tree != null) {
            checkNotVarDef(tree);
            scanInnerScope(parentScope, List.of(tree));
        }
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        log = tree.source.getLog();
        scan(tree.imports);
        scan(tree.constDefs);
        scan(tree.funcDefs);
        ensureRootScope();
        scanInnerScope(null, tree.stats);
    }

    @Override
    public void visitImport(Import tree) {
        // NO-OP yet
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        for (ConstDef.Definition def : tree.defs) {
            if (globalScope.isConstantDefined(def.name)) {
                log.error(def.name.pos, "constant redefinition");
                continue;
            }
            globalScope.defineUserConstant(def);
        }
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        if (globalScope.isFunctionDefined(tree.name)) {
            log.error(tree.name.pos, "function redefinition");
            return;
        }
        ensureRootScope();
        scope = new Scope(null);

        for (FuncDef.Parameter param : tree.params) {
            scope.declare(param.name);
            // Сканировать param.expr не нужно, поскольку это должен быть литерал.
        }

        switch (tree.body.getTag()) {
            case BLOCK:
                Block blockTree = (Block) tree.body;
                scan(blockTree.stats);
                break;
            case DISCARDED:
                Discarded discardedTree = (Discarded) tree.body;
                scan(discardedTree.expr);
                break;
            default:
                Assert.error(tree.body.getTag());
        }

        ensureScopeChainUnaffected(null);
        scope = null;

        globalScope.defineUserFunction(tree);
    }

    @Override
    public void visitBlock(Block tree) {
        scanInnerScope(scope, tree.stats);
    }

    @Override
    public void visitIf(If tree) {
        scan(tree.cond);
        scanBody(scope, tree.thenbody);
        scanBody(scope, tree.elsebody);
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        scan(tree.cond);
        scanBody(scope, tree.body);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        scanBody(scope, tree.body);
        scan(tree.cond);
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        Scope parentScope = scope;
        Scope initScope = scanInnerScope(parentScope, tree.init);
        scanInnerScope(initScope, List.of(tree.cond));
        scanBody(initScope, tree.body);
        scanInnerScope(initScope, tree.step);
        ensureScopeChainUnaffected(parentScope);
        scope = initScope.parent;
    }

    @Override
    public void visitCase(Case tree) {
        // Сканировать tree.labels не нужно, так как это должны быть литералы.
        scanBody(scope, tree.body);
    }

    @Override
    public void visitVarDef(VarDef tree) {
        for (VarDef.Definition def : tree.defs) {
            scope.declare(def.name);
            scan(def.init);
        }
    }

    @Override
    public void visitVariable(Var tree) {
        scope.lookup(tree.name);
    }
}
