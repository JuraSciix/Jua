package jua.compiler;

import jua.compiler.ModuleScope.FunctionSymbol;
import jua.compiler.ModuleScope.VarSymbol;
import jua.compiler.Tree.*;
import jua.compiler.utils.Assert;
import jua.compiler.utils.Flow;

import java.util.HashMap;

/**
 * Учет локальных переменных
 */
public class Enter extends Scanner {

    private static class VarScope extends HashMap<String, VarSymbol> {
        /** Родительская область. */
        private final VarScope parent;
        private int nextId = 0;
        int maxId = 0;
        VarScope(VarScope parent) {
            this.parent = parent;
            if (parent != null) {
                nextId = parent.nextId;
                maxId = parent.maxId;
            }
        }

        boolean defined(String name) {
            for (VarScope scope = this; scope != null; scope = scope.parent) {
                if (scope.containsKey(name)) {
                    return true;
                }
            }
            return false;
        }

        VarSymbol define(String name) {
            int id = nextId++;
            if (nextId > maxId) {
                // nextId и maxId у текущей области всегда >= чем у parent.
                // В таком случае, id больше maxId любой области текущей цепи.
                for (VarScope scope = this; scope != null; scope = scope.parent) {
                    scope.maxId = nextId;
                }
            }
            VarSymbol sym = new VarSymbol(id);
            put(name, sym);
            return sym;
        }

        VarSymbol resolve(String name) {
            for (VarScope scope = this; scope != null; scope = scope.parent) {
                // Халтурно избегаем двойного поиска (containsKey + get),
                // оставляя для ясности .getOrDefault(x, null)
                VarSymbol sym = scope.getOrDefault(name, null);
                if (sym != null) {
                    return sym;
                }
            }
            return null;
        }
    }

    private final ModuleScope globalScope;

    private final Log log;

    private Source source;

    /** Текущая область видимости. */
    private VarScope scope;

    public Enter(ModuleScope globalScope, Log log) {
        this.globalScope = globalScope;
        this.log = log;
    }

    private void ensureRootScope() {
        Assert.check(scope == null, "non-root scope");
    }

    private void ensureScopeChainUnaffected(VarScope parent) {
        Assert.check(scope.parent == parent, "scope chain has been affected");
    }

    private void report(int pos, String message) {
        log.error(source, pos, message);
    }

    private void checkNotVarDef(Tree tree) {
        if (tree.hasTag(Tag.VARDEF)) {
            report(tree.pos, "variable declaration isn't allowed here");
        }
    }

    private VarScope scanInnerScope(VarScope parentScope, Flow<? extends Tree> flow) {
        VarScope innerScope = new VarScope(parentScope);
        if (flow != null) {
            scope = innerScope;
            scan(flow);
            ensureScopeChainUnaffected(parentScope);
            scope = parentScope;
        }
        return innerScope;
    }

    private void scanBody(VarScope parentScope, Tree tree) {
        if (tree != null) {
            checkNotVarDef(tree);
            scanInnerScope(parentScope, Flow.of(tree));
        }
    }

    @Override
    public void visitDocument(Document tree) {
        ensureRootScope();
        source = tree.source;
        scan(tree.functions);
        scanInnerScope(null, tree.stats);
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        ensureRootScope();
        scope = new VarScope(null);

        Flow.forEach(tree.params, param -> {
            if (scope.defined(param.name)) {
                report(param.pos, "duplicated function parameter");
                // Так как это не позитивный случай, избегать двойного поиска (defined + resolve) нет смысла.
                param.sym = scope.resolve(param.name);
                return;
            }
            param.sym = scope.define(param.name);
            // Сканировать param.expr не нужно, поскольку это должен быть литерал.
        });

        if (globalScope.isFunctionDefined(tree.name)) {
            report(tree.namePos, "function redefinition");
            tree.sym = globalScope.lookupFunction(tree.name);
        } else {
            tree.sym = globalScope.defineUserFunction(tree, 0);
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

        tree.sym.nlocals = scope.maxId;

        ensureScopeChainUnaffected(null);
        scope = null;
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
        VarScope parentScope = scope;
        VarScope initScope = scanInnerScope(parentScope, tree.init);
        scanInnerScope(initScope, Flow.of(tree.cond));
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
        Flow.forEach(tree.defs, def -> {
            if (scope.defined(def.name)) {
                report(def.pos, "duplicated variable declaration");
                // Так как это не позитивный случай, избегать двойного поиска (defined + resolve) нет смысла.
                def.sym = scope.resolve(def.name);
                return;
            }
            def.sym = scope.define(def.name);
        });

        super.visitVarDef(tree);
    }

    @Override
    public void visitVariable(Var tree) {
        // Халтурно избегаем двойного поиска (defined + resolve)
        VarSymbol varSym = scope.resolve(tree.name);
        if (varSym != null) {
            tree.sym = varSym;
            return;
        }
        report(tree.pos, "undeclared variable");
    }
}
