package jua.compiler;

import jua.compiler.ProgramScope.FunctionSymbol;
import jua.compiler.Tree.*;

import static jua.compiler.TreeInfo.*;

public class Check extends Scanner {

    private final ProgramScope programScope;

    private final Log log;

    private boolean allowsBreak, allowsContinue, allowsFallthrough;

    public Check(ProgramScope programScope, Log log) {
        this.programScope = programScope;
        this.log = log;
    }

    @Override
    public void visitImport(Import tree) {
        log.error(tree.pos, "'use' statements are not supported yet");
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
                    log.error(stripParens(label).pos, "only literals are allowed as the case label expression");
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
            log.error(tree.pos, "break-statement is allowed only inside loop/switch-case");
        }
    }

    @Override
    public void visitContinue(Continue tree) {
        if (!allowsContinue) {
            log.error(tree.pos, "continue-statement is allowed only inside loop");
        }
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        if (!allowsFallthrough) {
            log.error(tree.pos, "fallthrough-statement is allowed only inside switch-case");
        }
    }

    @Override
    public void visitInvocation(Invocation tree) {
        // Заметка: Я не вызываю ниже stripParens потому что так надо
        Expression callee = tree.callee;
        if (!callee.hasTag(Tag.MEMACCESS) || ((MemberAccess) callee).expr != null) {
            log.error(stripParens(callee).pos, "only function calls are allowed");
            return;
        }

        Name calleeName = ((MemberAccess) callee).member;
        FunctionSymbol calleeSym = programScope.lookupFunction(calleeName);

        if (calleeSym == null) {
            log.error(tree.pos, "trying to call an undeclared function");
            return;
        }

        if (tree.args.count() > calleeSym.maxargs) {
            log.error(tree.pos, "cannot call function %s: too many arguments: %d total, %d passed", calleeSym.name, calleeSym.maxargs, tree.args.count());
            return;
        }

        if (tree.args.count() < calleeSym.minargs) {
            log.error(tree.pos, "cannot call function %s: too few arguments: %d required, %d passed", calleeSym.name, calleeSym.minargs, tree.args.count());
            return;
        }

        for (Invocation.Argument a : tree.args) {
            if (a.name != null) {
                if (calleeSym.paramnames != null && !calleeSym.paramnames.contains(a.name.value)) {
                    log.error(a.name.pos, "cannot call function %s: unrecognized function parameter name", calleeSym.name);
                    continue;
                }
                log.error(a.name.pos, "named arguments not yet supported");
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

    private boolean requireLiteralTree(Expression tree) {
        if (isLiteral(tree)) {
            return true;
        }
        log.error(stripParens(tree).pos, "only a literal values can be applied here");
        return false;
    }

    private boolean requireAccessibleTree(Expression tree) {
        if (isAccessible(tree)) {
            return true;
        }
        log.error(stripParens(tree).pos, "only an accessible values can be applied here");
        return false;
    }
}
