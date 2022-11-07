package jua.compiler;

import jua.compiler.Tree.*;

public class Enter extends Scanner {

    private final CodeLayout codeLayout;

    private final Log log;

    public Enter(CodeLayout codeLayout, Log log) {
        this.codeLayout = codeLayout;
        this.log = log;
    }

    @Override
    public void visitFuncDef(FuncDef tree) {
        if (codeLayout.testConstant(tree.name.value)) {
            log.error(tree.name.pos, "Function '" + tree.name + "' has been already declared.");
            return;
        }
        codeLayout.setFunction(tree.name.value, null);
    }

    @Override
    public void visitConstDef(ConstDef tree) {
        for (ConstDef.Definition def : tree.defs) {
            Name name = def.name;
            if (codeLayout.testConstant(name.value)) {
                log.error(name.pos, "Constant '" + name.value + "' has been already declared.");
                continue;
            }
            codeLayout.setConstant(name.value, null);
        }
    }
}
