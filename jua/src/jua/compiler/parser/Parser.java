package jua.compiler.parser;

import jua.compiler.Tree;

public interface Parser {

    Tree.CompilationUnit parse();

    Tree.Statement parseStatement();

    Tree.Expression parseExpression();
}
