package jua.compiler;

import jua.compiler.Tree.*;
import jua.util.Assert;

import java.util.HashSet;
import java.util.Objects;

import static jua.compiler.TreeInfo.*;

public class Check extends Scanner {

    private final ProgramLayout programLayout;

    private final Log log;

    private final HashSet<String> knownVars = new HashSet<>();

    private boolean inFunction = false;

    private boolean inLoop = false;

    private boolean inSwitchCase = false;

    public Check(ProgramLayout programLayout, Log log) {
        this.programLayout = Objects.requireNonNull(programLayout);
        this.log = Objects.requireNonNull(log);
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        scan(tree.stats);
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        for (ConstDef.Definition def : tree.defs) {
            if (!isLiteral(def.expr)) {
                log.error(def.expr.pos, "literal expected");
            }
        }
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        if (inFunction) {
//            log.error(tree.pos, "function declaration is not allowed here");
//            return;
            Assert.error();
        }

        for (FuncDef.Parameter param : tree.params) {
            Name name = param.name;
            if (!knownVars.add(name.value)) {
                log.error(name.pos, "duplicate function parameter");
                continue;
            }
            if (param.expr != null && !isLiteral(param.expr)) {
                log.error(stripParens(param.expr).pos,
                        "only literals are allowed as the default parameter value expression");
            }
        }

        inFunction = true;
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
        boolean prevInLoop = inLoop;
        inLoop = true;
        try {
            scan(tree);
        } finally {
            inLoop = prevInLoop;
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
        boolean prevInSwitchCase = inSwitchCase;
        inSwitchCase = true;
        try {
            scan(tree);
        } finally {
            inSwitchCase = prevInSwitchCase;
        }
    }

    @Override
    public void visitBreak(Break tree) {
        if (!inLoop && !inSwitchCase) {
            log.error(tree.pos, "break-statement is allowed only inside loop/switch-case");
        }
    }

    @Override
    public void visitContinue(Continue tree) {
        if (!inLoop) {
            log.error(tree.pos, "continue-statement is allowed only inside loop");
        }
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        if (!inSwitchCase) {
            log.error(tree.pos, "fallthrough-statement is allowed only inside switch-case");
        }
    }

    @Override
    public void visitVariable(Var tree) {
        Name name = tree.name;
        if (!programLayout.hasConstant(name) && knownVars.add(name.value)) {
            log.error(name.pos, "attempt to refer to an undefined variable");
        }
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

        int argc = tree.args.count();
        if (calleeTree.member.value.equals("length")) {
            if (argc != 1) {
                log.error(tree.pos, "the function 'length' takes a single parameter");
                return;
            }
            if (tree.args.first().name != null) {
                log.error(tree.args.first().name.pos, "undefined function param name");
            }
            return;
        }
        ProgramLayout.FuncData fd;
        if ((fd = programLayout.tryFindFunc(calleeTree.member)) == null) {
            log.error(calleeTree.member.pos, "trying to call an undefined function");
            return;
        }
        // fd.maxargs always less than 256
//            if (argc > 255) {
//                log.error(tree.pos, "the number of call arguments cannot exceed 255");
//                return;
//            }
        if (argc < fd.minargs) {
            log.error(calleeTree.pos, "too few arguments: %d required, %d passed", fd.minargs, argc);
            return;
        }
        if (argc > fd.maxargs) {
            log.error(calleeTree.pos, "too many arguments: %d total, %d passed", fd.maxargs, argc);
            return;
        }
        boolean safe = true;

        for (Invocation.Argument a : tree.args) {
            Name name = a.name;
            if (name != null) {
                if (!fd.paramnames.contains(name.value)) {
                    log.error(name.pos, "undefined function param name");
                    safe = false;
                    continue;
                }
                log.error(name.pos, "named arguments not yet supported");
                continue;
            }
            scan(a.expr);
        }

        tree._safe = safe;
    }

    @Override
    public void visitAssign(Assign tree) {
        Expression inner_var = stripParens(tree.var);

        if (inner_var.hasTag(Tag.VARIABLE)) {
            scan(tree.expr);
            Var varTree = (Var) inner_var;
            knownVars.add(varTree.name.value);
        } else {
            if (!isAccessible(tree.var)) {
                log.error(inner_var.pos, "attempt to assign a value to a non-accessible expression");
            } else {
                scan(tree.var);
            }
            scan(tree.expr);
        }
    }

    @Override
    public void visitCompoundAssign(CompoundAssign tree) {
        Expression innerVar = stripParens(tree.var);

        if (!isAccessible(innerVar)) {
            log.error(innerVar.pos, "attempt to assign a value to a non-accessible expression");
        } else {
            scan(tree.var);
        }
        scan(tree.expr);
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        switch (tree.getTag()) {
            case POSTINC: case POSTDEC:
            case PREINC: case PREDEC:
                Expression expr = stripParens(tree.expr);
                if (!isAccessible(expr)) {
                    log.error(expr.pos, "the increment operation is allowed only on accessible expressions");
                    break;
                }
        }
        scan(tree.expr);
    }
}
