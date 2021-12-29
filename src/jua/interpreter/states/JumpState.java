package jua.interpreter.states;

import jua.interpreter.Environment;
import jua.tools.CodePrinter;

public abstract class JumpState implements State {

    protected int destination;

    @Override
    public void print(CodePrinter printer) {
        printer.printIp(destination);
    }

    @Override
    public int run(Environment env) {
        throw new IllegalStateException("run(Environment) not implemented by " + getClass().getName());
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }
}