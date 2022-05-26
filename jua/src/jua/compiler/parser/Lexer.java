package jua.compiler.parser;

import java.util.Iterator;
import java.util.Queue;

public interface Lexer {

    Queue<Tokens.Token> lex();

    Iterator<Tokens.Token> asIterator();
}
