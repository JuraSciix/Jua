package jua.compiler;

public class Enter extends Tree.Analyzer {
    private final CodeData codeData;

    public Enter(CodeData codeData) {
        this.codeData = codeData;
    }

    @Override
    public void visitPrintln(Tree.PrintlnStatement statement) {
        //todo: удали
    }

    @Override
    public void visitPrint(Tree.PrintStatement statement) {
        //todo: удали
    }

    @Override
    public void visitFunctionDecl(Tree.FunctionDecl tree) {
        if (codeData.testConstant(tree.name))
            throw new CompileError("Function '" + tree.name + "' redeclare", tree.pos);
        codeData.setFunction(tree.name, null);
    }

    @Override
    public void visitCompilationUnit(Tree.CompilationUnit tree) {
        // todo
    }
}
