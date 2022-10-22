package jua.compiler;

import jua.compiler.Tree.*;

import java.util.Iterator;
import java.util.List;

public final class TreeComparator extends Scanner {

    private Source source;

    private Tree parameter;

    private boolean result;

    public boolean compare(Tree tree, Tree parameter) {
        if (tree == parameter) return true;
        if (tree == null || parameter == null || tree.getTag() != parameter.getTag()) return false;

        Tree prevParameter = this.parameter;
        boolean prevResult = result;

        try {
            this.parameter = parameter;
            tree.accept(this);
            return result;
        } finally {
            this.parameter = prevParameter;
            result = prevResult;
        }
    }

    public boolean compare(List<? extends Tree> trees, List<? extends Tree> parameters) {
        if (trees == parameters) return true;
        if (trees == null || parameters == null || trees.size() != parameters.size()) return false;

        Iterator<? extends Tree> treeItr = trees.iterator();
        Iterator<? extends Tree> parameterItr = parameters.iterator();

        while (treeItr.hasNext()) {
            Tree tree = treeItr.next();
            Tree parameter = parameterItr.next();
            if (!compare(tree, parameter)) return false;
        }

        return true;
    }

    @Override
    public void visitCompilationUnit(CompilationUnit tree) {
        if (source != null) {
            throw new IllegalStateException();
        }
        CompilationUnit that = (CompilationUnit) parameter;

        try {
            source = tree.source;
            result = compare(tree.trees, that.trees);
        } finally {
            source = null;
        }
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        ConstDef that = (ConstDef) parameter;

        boolean aResult = false;

        if (tree.defs.size() == that.defs.size()) {
            aResult = true;

            Iterator<ConstDef.Definition> defItr1 = tree.defs.iterator();
            Iterator<ConstDef.Definition> defItr2 = that.defs.iterator();

            while (defItr1.hasNext()) {
                ConstDef.Definition def1 = defItr1.next();
                ConstDef.Definition def2 = defItr2.next();
                if (!TreeInfo.isNameEquals(def1.name, def2.name) || !compare(def1.expr, def2.expr)) {
                    aResult = false;
                    break;
                }
            }
        }

        result = aResult;
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        FuncDef that = (FuncDef) parameter;

        boolean aResult = false;

        if (TreeInfo.isNameEquals(tree.name, that.name) && tree.params.size() == that.params.size()) {
            aResult = true;

            Iterator<FuncDef.Parameter> paramItr1 = tree.params.iterator();
            Iterator<FuncDef.Parameter> paramItr2 = that.params.iterator();

            while (paramItr1.hasNext()) {
                FuncDef.Parameter param1 = paramItr1.next();
                FuncDef.Parameter param2 = paramItr2.next();

                if (!TreeInfo.isNameEquals(param1.name, param2.name) || !compare(param1.expr, param2.expr)) {
                    aResult = false;
                    break;
                }
            }

            if (aResult && !compare(tree.body, that.body)) {
                aResult = false;
            }
        }

        result = aResult;
    }

    @Override
    public void visitBlock(Block tree) {
        Block that = (Block) parameter;
        result = compare(tree.stats, that.stats);
    }

    @Override
    public void visitIf(If tree) {
        If that = (If) parameter;
        result = compare(tree.cond, that.cond) &&
                compare(tree.thenbody, that.thenbody) &&
                compare(tree.elsebody, that.elsebody);
    }

    @Override
    public void visitWhileLoop(WhileLoop tree) {
        WhileLoop that = (WhileLoop) parameter;
        result = compare(tree.cond, that.cond) && compare(tree.body, that.body);
    }

    @Override
    public void visitFor(ForLoop tree) {
        ForLoop that = (ForLoop) parameter;
        result = compare(tree.init, that.init) &&
                compare(tree.cond, that.cond) &&
                compare(tree.step, that.step) &&
                compare(tree.body, that.body);
    }

    @Override
    public void visitDoLoop(DoLoop tree) {
        DoLoop that = (DoLoop) parameter;
        result = compare(tree.body, that.body) && compare(tree.cond, that.cond);
    }

    @Override
    public void visitSwitch(Switch tree) {
        Switch that = (Switch) parameter;
        result = compare(tree.expr, that.expr) && compare(tree.cases, that.cases);
    }

    @Override
    public void visitCase(Case tree) {
        Case that = (Case) parameter;
        result = compare(tree.labels, that.labels) && compare(tree.body, that.body);
    }

    @Override
    public void visitBreak(Break tree) {
        result = true;
    }

    @Override
    public void visitContinue(Continue tree) {
        result = true;
    }

    @Override
    public void visitFallthrough(Fallthrough tree) {
        result = true;
    }

    @Override
    public void visitReturn(Return tree) {
        Return that = (Return) parameter;
        result = compare(tree.expr, that.expr);
    }

    @Override
    public void visitDiscarded(Discarded tree) {
        Discarded that = (Discarded) parameter;
        result = compare(tree.expr, that.expr);
    }

    @Override
    public void visitLiteral(Literal tree) {
        Literal that = (Literal) parameter;
        result = tree.type.equals(that.type);
    }

    @Override
    public void visitArrayLiteral(ArrayLiteral tree) {
        super.visitArrayLiteral(tree);
    }

    @Override
    public void visitVariable(Var tree) {
        Var that = (Var) parameter;
        result = TreeInfo.isNameEquals(tree.name, that.name);
    }

    @Override
    public void visitArrayAccess(ArrayAccess tree) {
        ArrayAccess that = (ArrayAccess) parameter;
        result = compare(tree.expr, that.expr) && compare(tree.index, that.index);
    }

    @Override
    public void visitInvocation(Invocation tree) {
        Invocation that = (Invocation) parameter;
        boolean aResult = false;

        if (TreeInfo.isNameEquals(tree.name, that.name) && tree.args.size() == that.args.size()) {
            aResult = true;

            Iterator<Invocation.Argument> argItr1 = tree.args.iterator();
            Iterator<Invocation.Argument> argItr2 = that.args.iterator();

            while (argItr1.hasNext()) {
                Invocation.Argument arg1 = argItr1.next();
                Invocation.Argument arg2 = argItr2.next();

                if (!TreeInfo.testNamedArgument(source.target(), arg1.name, arg2.name) || !compare(arg1.expr, arg2.expr)) {
                    aResult = false;
                    break;
                }
            }
        }

        result = aResult;
    }

    @Override
    public void visitParens(Parens tree) {
        Parens that = (Parens) parameter;
        result = compare(tree.expr, that.expr);
    }

    @Override
    public void visitAssignOp(AssignOp tree) {
        AssignOp that = (AssignOp) parameter;
        result = compare(tree.dst, that.dst) && compare(tree.src, that.src);
    }

    @Override
    public void visitTernaryOp(TernaryOp tree) {
        TernaryOp that = (TernaryOp) parameter;
        result = compare(tree.cond, that.cond) &&
                compare(tree.thenexpr, that.thenexpr) &&
                compare(tree.elseexpr, that.elseexpr);
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        BinaryOp that = (BinaryOp) parameter;
        result = compare(tree.lhs, that.lhs) && compare(tree.rhs, that.rhs);
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        UnaryOp that = (UnaryOp) parameter;
        result = compare(tree.expr, that.expr);
    }
}
