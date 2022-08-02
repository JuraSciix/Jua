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
    public void visitConstantDeclare(Tree.ConstantDecl tree) {
        for (Tree.Definition def : tree.definitions) {
            if (codeData.testConstant(def.name.value)) {
                throw new CompileError("Constant '" + def.name.value + "' redeclare", def.name.pos);
            }
            codeData.setConstant(def.name.value, null);
        }
    }
}
