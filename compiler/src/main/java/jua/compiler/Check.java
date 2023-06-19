package jua.compiler;

import jua.compiler.ProgramScope.FunctionSymbol;
import jua.compiler.Tree.*;
import jua.utils.Assert;

import static jua.compiler.TreeInfo.*;

public class Check extends Scanner {

    private final ProgramScope programScope;

    private final Log log;

    private Source source;

    private boolean allowsBreak, allowsContinue, allowsFallthrough;

    public Check(ProgramScope programScope, Log log) {
        this.programScope = programScope;
        this.log = log;
    }
    
    private void report(int pos, String message) {
        log.error(source, pos, message);
    }

    private void report(int pos, String message, Object... args) {
        log.error(source, pos, message, args);
    }

    private boolean requireLiteralTree(Expression tree) {
        if (isLiteral(tree)) {
            return true;
        }
        report(stripParens(tree).pos, "only a literal values can be applied here");
        return false;
    }

    private boolean requireAccessibleTree(Expression tree) {
        if (isAccessible(tree)) {
            return true;
        }
        report(stripParens(tree).pos, "only an accessible values can be applied here");
        return false;
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        source = tree.source;
        scan(tree.imports);
        scan(tree.constants);
        scan(tree.functions);
        scan(tree.stats);
    }

    @Override
    public void visitImport(Import tree) {
        report(tree.pos, "'use' statements are not supported yet");
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        for (ConstDef.Definition def : tree.defs) {
            requireLiteralTree(def.expr);
        }
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        for (FuncDef.Parameter param : tree.params) {
            if (param.expr != null) {
                requireLiteralTree(param.expr);
            }
        }

        scan(tree.body);
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        scan(tree.cond);
        scanLoopBody(tree.body);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        scanLoopBody(tree.body);
        scan(tree.cond);
    }

    @Override
    public void visitForLoop(ForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        scanLoopBody(tree.body);
        scan(tree.step);
    }

    private void scanLoopBody(Statement tree) {
        boolean prevAllowsBreak = allowsBreak;
        boolean prevAllowsContinue = allowsContinue;
        allowsBreak = true;
        allowsContinue = true;
        try {
            scan(tree);
        } finally {
            allowsBreak = prevAllowsBreak;
            allowsContinue = prevAllowsContinue;
        }
    }

    @Override
    public void visitCase(Case tree) {
        if (tree.labels != null) { // default-case?
            for (Expression label : tree.labels) {
                if (!isLiteral(label)) {
                    report(stripParens(label).pos, "only literals are allowed as the case label expression");
                }
            }
        }
        scanCaseBody(tree.body);
    }

    private void scanCaseBody(Statement tree) {
        boolean prevAllowsBreak = allowsBreak;
        boolean prevAllowsFallthrough = allowsFallthrough;
        allowsBreak = true;
        allowsFallthrough = true;
        try {
            scan(tree);
        } finally {
            allowsBreak = prevAllowsBreak;
            allowsFallthrough = prevAllowsFallthrough;
        }
    }

    @Override
    public void visitBreak(Break tree) {
        if (!allowsBreak) {
            report(tree.pos, "break-statement is allowed only inside loop/switch-case");
        }
    }

    @Override
    public void visitContinue(Continue tree) {
        if (!allowsContinue) {
            report(tree.pos, "continue-statement is allowed only inside loop");
        }
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        if (!allowsFallthrough) {
            report(tree.pos, "fallthrough-statement is allowed only inside switch-case");
        }
    }

    @Override
    public void visitInvocation(Invocation tree) {
        // Заметка: Я не вызываю ниже stripParens потому что так надо
        Expression callee = tree.target;
        if (!callee.hasTag(Tag.MEMACCESS) || ((MemberAccess) callee).expr != null) {
            report(stripParens(callee).pos, "only function calls are allowed");
            return;
        }

        Name calleeName = ((MemberAccess) callee).member;
        FunctionSymbol calleeSym = programScope.lookupFunction(calleeName);

        //
        Assert.checkNonNull(calleeSym);

        if (tree.args.count() > calleeSym.maxArgc) {
            report(tree.pos, "cannot call function %s: too many arguments: %d total, %d passed", calleeSym.name, calleeSym.maxArgc, tree.args.count());
            return;
        }

        if (tree.args.count() < calleeSym.minArgc) {
            report(tree.pos, "cannot call function %s: too few arguments: %d required, %d passed", calleeSym.name, calleeSym.minArgc, tree.args.count());
            return;
        }

        for (Invocation.Argument a : tree.args) {
            if (a.name != null) {
                if (calleeSym.params != null && !calleeSym.params.contains(a.name.toString())) {
                    report(a.name.pos, "cannot call function %s: unrecognized function parameter name", calleeSym.name);
                    continue;
                }
                report(a.name.pos, "named arguments not yet supported");
                continue;
            }
            scan(a.expr);
        }
    }

    @Override
    public void visitAssign(Assign tree) {
        if (requireAccessibleTree(tree.var)) {
            scan(tree.var);
        }
        scan(tree.expr);
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        if (requireAccessibleTree(tree.var)) {
            scan(tree.var);
        }
        scan(tree.expr);
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        switch (tree.getTag()) {
            case POSTINC: case POSTDEC:
            case PREINC: case PREDEC:
                if (requireAccessibleTree(tree.expr)) {
                    scan(tree.expr);
                }
                break;
            default:
                scan(tree.expr);
        }
    }
}