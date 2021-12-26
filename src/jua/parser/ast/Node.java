package jua.parser.ast;

public interface Node {

    void accept(Visitor visitor);
}
