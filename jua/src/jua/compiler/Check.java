package jua.compiler;

import jua.compiler.Tree.*;

import java.util.HashSet;

import static jua.compiler.TreeInfo.*;

public class Check extends Scanner {

    private final ProgramLayout programLayout;

    private final Log log;

    private boolean allowsBreak, allowsContinue, allowsFallthrough;

    private HashSet<String> scopedVars = new HashSet<>();

    public Check(ProgramLayout programLayout, Log log) {
        this.programLayout = programLayout;
        this.log = log;
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        scan(tree.stats);
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        for (ConstDef.Definition def : tree.defs) {
            if (!isLiteral(def.expr)) {
                log.error(stripParens(def.expr).pos, "literal expected");
            }
        }
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        for (FuncDef.Parameter param : tree.params) {
            boolean duplicate = !scopedVars.add(param.name.value);
            if (duplicate) {
                log.error(param.name.pos, "duplicate function parameter");
                continue;
            }
            if (param.expr != null && !isLiteral(param.expr)) {
                log.error(stripParens(param.expr).pos, "only literals are allowed as the default parameter value expression");
            }
        }

        scan(tree.body);
    }

    @Override
    public void visitBlock(Block tree) {
        // Переменные из внешнего scope видны во внутренних scope
        // При этом переменные из внутренних scope не видны во внешних
        HashSet<String> scopedVarsCopy = new HashSet<>(scopedVars);
        try {
            scan(tree.stats);
        } finally {
            scopedVars = scopedVarsCopy;
        }
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
            scanAtInnerScope(tree);
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
            scanAtInnerScope(tree);
        } finally {
            allowsBreak = prevAllowsBreak;
            allowsFallthrough = prevAllowsFallthrough;
        }
    }

    private void scanAtInnerScope(Tree tree) {
        // Переменные из внешнего scope видны во внутренних scope
        // При этом переменные из внутренних scope не видны во внешних
        HashSet<String> scopedVarsCopy = new HashSet<>(scopedVars);
        try {
            scan(tree);
        } finally {
            scopedVars = scopedVarsCopy;
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
    public void visitVarDef(VarDef tree) {
        for (VarDef.Definition def : tree.defs) {
            boolean duplicate = !scopedVars.add(def.name.value);
            if (duplicate) {
                log.error(def.name.pos, "duplicate variable definition");
            }
            scan(def.init);
        }
    }

    @Override
    public void visitVariable(Var tree) {
        Name name = tree.name;
        if (!programLayout.hasConstant(name) && scopedVars.add(name.value)) {
            log.error(name.pos, "attempt to refer to an undefined variable");
        }
        // todo: проверять инициализацию переменных, убрав проверку из рантайма
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
    }

    @Override
    public void visitAssign(Assign tree) {
        Expression inner_var = stripParens(tree.var);

        if (!isAccessible(inner_var)) {
            log.error(inner_var.pos, "attempt to assign a value to a non-accessible expression");
        } else {
            scan(tree.var);
        }
        scan(tree.expr);
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
                Expression inner_expr = stripParens(tree.expr);
                if (!isAccessible(inner_expr)) {
                    log.error(inner_expr.pos, "the increment operation is allowed only on accessible expressions");
                    return;
                }
        }
        scan(tree.expr);
    }
}
