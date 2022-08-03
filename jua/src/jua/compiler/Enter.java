package jua.compiler;

import jua.compiler.Tree.*;

public class Enter extends Scanner {

    private final CodeData codeData;

    public Enter(CodeData codeData) {
        this.codeData = codeData;
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        if (codeData.testConstant(tree.name.value))
            throw new CompileError("Function '" + tree.name + "' has been already declared.", tree.pos);
        codeData.setFunction(tree.name.value, null);
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        for (Definition def : tree.defs) {
            Name name = def.name;
            if (codeData.testConstant(name.value)) {
                throw new CompileError("Constant '" + name.value + "' has been already declared.", name.pos);
            }
            codeData.setConstant(name.value, null);
        }
    }
}
