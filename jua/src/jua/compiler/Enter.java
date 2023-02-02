package jua.compiler;

import jua.compiler.ProgramScope.FunctionSymbol;
import jua.compiler.ProgramScope.VarSymbol;
import jua.compiler.Tree.*;
import jua.utils.Assert;
import jua.utils.List;

import java.util.HashMap;

/**
 * Учет локальных переменных
 */
public class Enter extends Scanner {

    private static class Scope {

        /** Родительская (внешняя) область. */
        final Scope parent;

        final HashMap<Name, VarSymbol> varSymbols = new HashMap<>();

        int nextVarId;

        Scope(Scope parent) {
            this.parent = parent;

            if (parent != null) {
                nextVarId = parent.nextVarId;
            }
        }

        boolean defined(Name name) {
            for (Scope scope = this; scope != null; scope = scope.parent) {
                if (scope.varSymbols.containsKey(name)) {
                    return true;
                }
            }
            return false;
        }

        VarSymbol define(Name name) {
            int id = nextVarId++;
            VarSymbol sym = new VarSymbol(id);
            varSymbols.put(name, sym);
            return sym;
        }

        VarSymbol resolve(Name name) {
            for (Scope scope = this; scope != null; scope = scope.parent) {
                // Халтурно избегаем двойного поиска (containsKey + get),
                // оставляя для ясности .getOrDefault(x, null)
                VarSymbol sym = scope.varSymbols.getOrDefault(name, null);
                if (sym != null) {
                    return sym;
                }
            }
            return null;
        }
    }

    private final ProgramScope globalScope;

    private Source source;

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

    private void report(int pos, String message) {
        source.log.error(source, pos, message);
    }

    private void checkNotVarDef(Tree tree) {
        if (tree.hasTag(Tag.VARDEF)) {
            report(tree.pos, "variable declaration isn't allowed here");
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
        source = tree.source;
        scan(tree.imports);

        for (ConstDef constant : tree.constants) {
            for (ConstDef.Definition def : constant.defs) {
                if (globalScope.isConstantDefined(def.name)) {
                    report(def.name.pos, "constant redefinition");
                    def.sym = globalScope.lookupConstant(def.name);
                    continue;
                }
                def.sym = globalScope.defineUserConstant(def);
            }
        }

        scan(tree.constants);

        for (FuncDef function : tree.functions) {
            if (globalScope.isFunctionDefined(function.name)) {
                report(function.name.pos, "function redefinition");
                function.sym = globalScope.lookupFunction(function.name);
                continue;
            }
            function.sym = globalScope.defineUserFunction(function);
        }

        scan(tree.functions);

        ensureRootScope();
        scanInnerScope(null, tree.stats);

        tree.sym = new FunctionSymbol(
                "<main>",
                -1,
                0,
                0,
                List.empty()
        );
    }

    @Override
    public void visitImport(Import tree) {
        // NO-OP yet
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        ensureRootScope();
        scope = new Scope(null);

        for (FuncDef.Parameter param : tree.params) {
            if (scope.defined(param.name)) {
                report(param.name.pos, "duplicated function parameter");
                // Так как это не позитивный случай, избегать двойного поиска (defined + resolve) нет смысла.
                param.sym = scope.resolve(param.name);
                continue;
            }
            param.sym = scope.define(param.name);
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
            if (scope.defined(def.name)) {
                report(def.name.pos, "duplicated variable declaration");
                // Так как это не позитивный случай, избегать двойного поиска (defined + resolve) нет смысла.
                def.sym = scope.resolve(def.name);
                continue;
            }
            def.sym = scope.define(def.name);
            scan(def.init);
        }
    }

    @Override
    public void visitInvocation(Invocation tree) {
        if (tree.target.hasTag(Tag.MEMACCESS)) {
            MemberAccess targetTree = (MemberAccess) tree.target;
            if (targetTree.expr == null) {
                FunctionSymbol targetSym = globalScope.lookupFunction(targetTree.member);
                if (targetSym == null) {
                    tree.sym = globalScope.defineStubFunction(targetTree.member);
                    report(tree.pos, "calling an undeclared function");
                    return;
                }
                tree.sym = targetSym;
            }
        }

        for (Invocation.Argument a : tree.args) {
            scan(a.expr);
        }
    }

    @Override
    public void visitVariable(Var tree) {
        // Халтурно избегаем двойного поиска (defined + resolve)
        VarSymbol varSym = scope.resolve(tree.name);
        if (varSym != null) {
            tree.sym = varSym;
            return;
        }
        // Халтурно избегаем двойного поиска (isConstantDefined + lookupConstant)
        ProgramScope.ConstantSymbol constSym = globalScope.lookupConstant(tree.name);
        if (constSym != null) {
            tree.sym = constSym;
            return;
        }
        report(tree.pos, "undeclared variable");
    }
}
