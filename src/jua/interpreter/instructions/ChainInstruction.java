package jua.interpreter.instructions;

import jua.interpreter.InterpreterRuntime;
import jua.tools.CodePrinter;

public abstract class ChainInstruction implements Instruction {

    protected int destination;

    @Override
    public void print(CodePrinter printer) {
        printer.printIp(destination);
    }

    @Override
    public int run(InterpreterRuntime env) {
        throw new IllegalStateException("run(Environment) not implemented by " + getClass().getName());
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }
}