package jua.compiler;

import jua.compiler.Tree.CompilationUnit;
import jua.compiler.Tree.Expression;
import jua.compiler.Tree.FuncDef;
import jua.compiler.Tree.Statement;

public interface Parser {

    CompilationUnit parseCompilationUnit();

    FuncDef parseFunctionDeclare();

    Statement parseStatement();

    Expression parseExpression();
}
