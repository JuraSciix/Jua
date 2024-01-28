package jua.compiler;

import jua.compiler.ModuleScope.FunctionSymbol;
import jua.compiler.Tree.*;
import jua.compiler.utils.Assert;
import jua.compiler.utils.Flow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static jua.compiler.TreeInfo.*;

public class Check extends Scanner {

    private final ModuleScope programScope;

    private final Log log;

    private Source source;

    private boolean allowsBreak, allowsContinue, allowsFallthrough;

    public Check(ModuleScope programScope, Log log) {
        this.programScope = programScope;
        this.log = log;
    }
    
    private void report(int pos, String message) {
        log.error(source, pos, message);
    }

    private void report(int pos, String message, Object... args) {
        log.error(source, pos, message, args);
    }

    private boolean requireLiteralTree(Expr tree) {
        if (isLiteral(tree)) {
            return true;
        }
        report(stripParens(tree).pos, "only a literal values can be applied here");
        return false;
    }

    private boolean requireAccessibleTree(Expr tree) {
        if (isAccessible(tree)) {
            return true;
        }
        report(stripParens(tree).pos, "only an accessible values can be applied here");
        return false;
    }

    @Override
    public void visitDocument(Document tree) {
        source = tree.source;
        scan(tree.functions);
        scan(tree.stats);
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        Flow.forEach(tree.params, param -> {
            if (param.expr != null) {
                requireLiteralTree(param.expr);
            }
        });

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

    @Override
    public void visitSwitch(Switch tree) {
        super.visitSwitch(tree);

        Set<Object> dejaVu = new HashSet<>();
        Flow.forEach(tree.cases, c -> {
            if (c.labels == null) {
                if (!dejaVu.add(null)) {
                    report(c.pos, "duplicate default-case");
                }
            } else {
                Flow.forEach(c.labels, label -> {
                    Literal l = (Literal) TreeInfo.stripParens(label);
                    if (!dejaVu.add(l.value)) {
                        report(label.pos, "duplicate label");
                    }
                });
            }
        });
    }

    private void scanLoopBody(Stmt tree) {
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
            Flow.forEach(tree.labels, label -> {
                if (!isLiteral(label)) {
                    report(stripParens(label).pos, "only literals are allowed as the case label expression");
                }
            });
        }
        scanCaseBody(tree.body);
    }

    private void scanCaseBody(Stmt tree) {
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
        if (tree.target != null) {
            requireLiteralTree(tree.target);
        }
    }

    @Override
    public void visitInvocation(Invocation tree) {
        // Заметка: Я не вызываю ниже stripParens потому что так надо
        Expr callee = tree.target;
        if (!callee.hasTag(Tag.MEMACCESS) || ((MemberAccess) callee).expr != null) {
            report(stripParens(callee).pos, "only function calls are allowed");
            return;
        }

        String calleeName = ((MemberAccess) callee).member;
        FunctionSymbol calleeSym = programScope.lookupFunction(calleeName);

        //
        Assert.checkNonNull(calleeSym);

        int count = Flow.count(tree.args);
        if (count > calleeSym.hiargc) {
            report(tree.pos, "cannot call function %s: too many arguments: %d total, %d passed", calleeSym.name, calleeSym.hiargc, count);
            return;
        }

        if (count < calleeSym.loargc) {
            report(tree.pos, "cannot call function %s: too few arguments: %d required, %d passed", calleeSym.name, calleeSym.loargc, count);
            return;
        }

        Flow.forEach(tree.args, a -> {
            if (a.name != null) {
                if (calleeSym.params != null && !Arrays.asList(calleeSym.params).contains(a.name)) {
                    report(a.pos, "cannot call function %s: unrecognized function parameter name", calleeSym.name);
                    return;
                }
                report(a.pos, "named arguments not yet supported");
                return;
            }
            scan(a.expr);
        });
    }

    @Override
    public void visitAssign(Assign tree) {
        if (requireAccessibleTree(tree.var)) {
            scan(tree.var);
        }
        scan(tree.expr);
    }

    @Override
    public void visitEnhancedAssign(EnhancedAssign tree) {
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
