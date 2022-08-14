package jua.compiler;

import jua.compiler.Tree.*;

public class Enter extends Scanner {

    private final CodeLayout codeLayout;

    public Enter(CodeLayout codeLayout) {
        this.codeLayout = codeLayout;
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        if (codeLayout.testConstant(tree.name.value))
            throw new CompileError("Function '" + tree.name + "' has been already declared.", tree.pos);
        codeLayout.setFunction(tree.name.value, null);
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        for (ConstDef.Definition def : tree.defs) {
            Name name = def.name;
            if (codeLayout.testConstant(name.value)) {
                throw new CompileError("Constant '" + name.value + "' has been already declared.", name.pos);
            }
            codeLayout.setConstant(name.value, null);
        }
    }
}
